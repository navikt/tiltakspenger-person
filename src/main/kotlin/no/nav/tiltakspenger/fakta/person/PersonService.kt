package no.nav.tiltakspenger.fakta.person

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class PersonService(rapidsConnection: RapidsConnection):
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
        log.error { "Lest melding : ${packet.toJson()}" }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
