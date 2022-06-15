package no.nav.tiltakspenger.fakta.person

import kotlinx.coroutines.*
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.withMDC
import no.nav.tiltakspenger.fakta.person.pdl.PDLClient
import no.nav.tiltakspenger.fakta.person.pdl.PDLClientError

private val log = KotlinLogging.logger {}
class PersonService(rapidsConnection: RapidsConnection, val pdlClient: PDLClient = PDLClient()):
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
            log.info { "her skal vi gjøre et kall til pdl med fnr $fnr" }

            // Consider not using runBlocking
            runBlocking {
                    pdlClient.hentPerson(fnr)
            }
                .mapLeft(::håndterFeil)
                .map {
                    val løsning = it.toMap()
                    packet["@løsning"] = løsning
                    log.info { "Løst behov for behov $behovId" }
                    log.info { "Vi skal sende ${packet.toJson()}" }
                    context.publish(packet.toJson())
                }

        }
    }

    fun håndterFeil(clientError: PDLClientError) {
        when (clientError) {
            is PDLClientError.FantIkkePerson -> log.error { "Fant ikke person" }
            is PDLClientError.UkjentFeil -> log.error { clientError.errors }
            is PDLClientError.NavnKunneIkkeAvklares -> log.error { "Navn kunne ikke avklares" }
            is PDLClientError.NetworkError -> log.error { clientError.exception }
        }
    }
}
