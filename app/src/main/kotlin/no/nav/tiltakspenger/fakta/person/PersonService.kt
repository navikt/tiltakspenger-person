package no.nav.tiltakspenger.fakta.person

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.withMDC
import no.nav.tiltakspenger.fakta.person.domain.models.Feilmelding
import no.nav.tiltakspenger.fakta.person.domain.models.Respons
import no.nav.tiltakspenger.fakta.person.pdl.PDLClient
import no.nav.tiltakspenger.fakta.person.pdl.PDLClientError

private val log = KotlinLogging.logger {}
class PersonService(rapidsConnection: RapidsConnection, val pdlClient: PDLClient = PDLClient()) :
    River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAllOrAny("@behov", listOf("person"))
                it.forbid("@løsning")
                it.requireKey("@id", "@behovId")
                it.requireArray("identer") {
                    requireKey("type", "historisk", "id")
                }
                it.require("identer") { identer ->
                    if (!identer.any { ident ->
                        ident["type"].asText() == "fnr"
                    }
                    ) throw IllegalArgumentException("Mangler fnr i identer")
                }
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val behovId = packet["@behovId"]
        withMDC(
            "behovId" to packet["@behovId"].asText(),
        ) {
            val fnr = packet["identer"]
                .first {
                    it["type"]
                        .asText() == "fnr" && !it["historisk"]
                        .asBoolean()
                }["id"]
                .asText()

            runBlocking {
                pdlClient.hentPerson(fnr)
            }
                .mapLeft {
                    håndterFeil(
                        clientError = it,
                        context = context,
                        packet = packet,
                    )
                }
                .map { person ->
                    packet["@løsning"] = Respons(person = person)
                    log.info { "Løst behov for behov $behovId" }
                    log.info { "Vi skal sende ${packet.toJson()}" }
                    context.publish(packet.toJson())
                }
        }
    }
    private fun håndterFeil(clientError: PDLClientError, context: MessageContext, packet: JsonMessage) {
        when (clientError) {
            is PDLClientError.UkjentFeil -> {
                log.error { clientError.errors }
                throw IllegalStateException("Ukjent feil")
            }
            PDLClientError.NavnKunneIkkeAvklares -> {
                log.error { "Navn kunne ikke avklares, DETTE SKAL IKKE SKJE" }
                throw IllegalStateException("Navn kunne ikke avklares")
            }
            is PDLClientError.NetworkError -> {
                log.error { clientError.exception }
                throw IllegalStateException("PDL er nede!!")
            }
            PDLClientError.IngenNavnFunnet -> {
                log.error { "Fant ingen navn i PDL, DETTE SKAL IKKE SKJE" }
                throw IllegalStateException("Fant ingen navn i PDL")
            }
            PDLClientError.ResponsManglerPerson -> {
                log.error { "Respons fra PDL inneholdt ikke person" }
                packet["@løsning"] = Respons(feil = Feilmelding.PersonIkkeFunnet)
                context.publish(packet.toJson())
            }
            is PDLClientError.SerializationException -> {
                log.error { "Could not serialize response" }
                log.error { clientError.exception }
                throw IllegalStateException("Feil ved serializering")
            }
            PDLClientError.GraderingKunneIkkeAvklares -> {
                log.error { "Kunne ikke avklare gradering" }
                throw IllegalStateException("Kunne ikke avklare gradering")
            }
        }
    }
}
