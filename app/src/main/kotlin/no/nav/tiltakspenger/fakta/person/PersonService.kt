package no.nav.tiltakspenger.fakta.person

import arrow.core.getOrHandle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import mu.KotlinLogging
import mu.withLoggingContext
import net.logstash.logback.argument.StructuredArguments
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.tiltakspenger.fakta.person.domain.models.Feilmelding
import no.nav.tiltakspenger.fakta.person.domain.models.Respons
import no.nav.tiltakspenger.fakta.person.pdl.PDLClient
import no.nav.tiltakspenger.fakta.person.pdl.PDLClientError

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class PersonService(
    rapidsConnection: RapidsConnection,
    val pdlClient: PDLClient = PDLClient()
) : River.PacketListener {

    companion object {
        internal object BEHOV {
            const val PERSONDATA = "persondata"
        }
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAllOrAny("@behov", listOf(BEHOV.PERSONDATA))
                it.forbid("@løsning")
                it.requireKey("@id", "@behovId")
                it.requireKey("ident")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        runCatching {
            loggVedInngang(packet)

            val respons = withLoggingContext(
                "id" to packet["@id"].asText(),
                "behovId" to packet["@behovId"].asText()
            ) {
                val fnr = packet["ident"].asText()
                runBlocking(MDCContext()) {
                    pdlClient.hentPerson(fnr)
                }.map { person ->
                    Respons(person = person)
                }.getOrHandle { håndterFeil(it) }
            }

            packet["@løsning"] = mapOf(
                BEHOV.PERSONDATA to respons
            )
            loggVedUtgang(packet) { "$respons}" }
            context.publish(packet.toJson())
        }.onFailure {
            loggVedFeil(it, packet)
        }.getOrThrow()
    }

    @Suppress("ThrowsCount", "UseCheckOrError")
    private fun håndterFeil(clientError: PDLClientError): Respons {
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
                return Respons(feil = Feilmelding.PersonIkkeFunnet)
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

    fun loggVedInngang(packet: JsonMessage) {
        LOG.info(
            "løser behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText())
        )
        SECURELOG.info(
            "løser behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText())
        )
        LOG.info { "mottok melding: ${packet.toJson()}" }
    }

    private fun loggVedUtgang(packet: JsonMessage, løsning: () -> String) {
        LOG.info(
            "har løst behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText())
        )
        SECURELOG.info(
            "har løst behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText())
        )
        LOG.info { "publiserer løsning: $løsning" }
        LOG.info { "publiserer løsning: ${packet.toJson()}" }
    }

    private fun loggVedFeil(ex: Throwable, packet: JsonMessage) {
        LOG.error(
            "feil ved behandling av behov med {}, se securelogs for detaljer",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        SECURELOG.error(
            "feil ${ex.message} ved behandling av behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
            ex
        )
    }
}
