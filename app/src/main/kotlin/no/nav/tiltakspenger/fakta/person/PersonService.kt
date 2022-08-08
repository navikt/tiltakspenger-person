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

private val LOG = KotlinLogging.logger {}

class PersonService(rapidsConnection: RapidsConnection, val pdlClient: PDLClient = PDLClient()) :
    River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAllOrAny("@behov", listOf("Persondata"))
                it.forbid("@løsning")
                it.requireKey("@id", "@behovId")
                it.requireArray("identer") {
                    requireKey("type", "historisk", "id")
                }
                // TODO: Bør skrives om til å motta "ident" som alltid er et fnr, ikke den strukturen vi har her nå.
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
                    LOG.info { "Løst behov for behov $behovId" }
                    context.publish(packet.toJson())
                }
        }
    }

    private fun håndterFeil(clientError: PDLClientError, context: MessageContext, packet: JsonMessage) {
        when (clientError) {
            is PDLClientError.UkjentFeil -> {
                LOG.error { clientError.errors }
                throw IllegalStateException("Uhåndtert feil")
            }

            PDLClientError.NavnKunneIkkeAvklares -> {
                LOG.error { "Navn kunne ikke avklares, DETTE SKAL IKKE SKJE" }
                throw IllegalStateException("Navn kunne ikke avklares")
            }

            is PDLClientError.NetworkError -> {
                throw IllegalStateException("PDL er nede!!", clientError.exception)
            }

            PDLClientError.IngenNavnFunnet -> {
                LOG.error { "Fant ingen navn i PDL, DETTE SKAL IKKE SKJE" }
                throw IllegalStateException("Fant ingen navn i PDL")
            }

            PDLClientError.FantIkkePerson,
            PDLClientError.ResponsManglerPerson,
            -> {
                LOG.error { "Respons fra PDL inneholdt ikke person" }
                packet["@løsning"] = Respons(feil = Feilmelding.PersonIkkeFunnet)
                context.publish(packet.toJson())
            }

            is PDLClientError.SerializationException -> {
                throw IllegalStateException("Feil ved serializering", clientError.exception)
            }

            PDLClientError.GraderingKunneIkkeAvklares -> {
                LOG.error { "Kunne ikke avklare gradering" }
                throw IllegalStateException("Kunne ikke avklare gradering")
            }

            is PDLClientError.AzureAuthFailureException -> {
                throw IllegalStateException("Kunne ikke autentisere mot Azure", clientError.exception)
            }
        }
    }
}
