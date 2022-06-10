package no.nav.tiltakspenger.fakta.person

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.withMDC

class PersonService(rapidsConnection: RapidsConnection) :
    River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAllOrAny("@behov", listOf("person"))
                it.forbid("@løsning")
                it.requireKey("@id", "@behovId")
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
            val løsning = """{"person": {"navn" : "Kåre Kropp"}}"""

            packet["@løsning"] = løsning
            log.info { "Løst behov for behov $behovId" }
            context.publish(packet.toJson())
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
