package no.nav.tiltakspenger.person

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
import no.nav.tiltakspenger.libs.person.Feilmelding
import no.nav.tiltakspenger.libs.person.PersonRespons
import no.nav.tiltakspenger.person.pdl.PDLClientError
import no.nav.tiltakspenger.person.pdl.PDLService

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class PersonopplysningerService(
    rapidsConnection: RapidsConnection,
    val pdlService: PDLService
) : River.PacketListener {

    companion object {
        internal object BEHOV {
            const val PERSONOPPLYSNINGER = "personopplysninger"
        }
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAllOrAny("@behov", listOf(BEHOV.PERSONOPPLYSNINGER))
                it.forbid("@løsning")
                it.requireKey("@id", "@behovId")
                it.requireKey("ident")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        runCatching {
            loggVedInngang(packet)

            withLoggingContext(
                "id" to packet["@id"].asText(),
                "behovId" to packet["@behovId"].asText()
            ) {
                val ident = packet["ident"].asText()
                val respons: PersonRespons = runBlocking(MDCContext()) {
                    pdlService.hentPerson(ident)
                }.map { person ->
                    PersonRespons(person = person)
                }.getOrHandle { håndterFeil(it) }

                packet["@løsning"] = mapOf(
                    BEHOV.PERSONOPPLYSNINGER to respons
                )
                loggVedUtgang(packet)
                context.publish(ident, packet.toJson())
            }
        }.onFailure {
            loggVedFeil(it, packet)
        }.getOrThrow()
    }

    private fun håndterFeil(clientError: PDLClientError): PersonRespons {
        when (clientError) {
            is PDLClientError.UkjentFeil -> {
                LOG.error { clientError.errors }
                throw IllegalStateException("Uhåndtert feil")
            }

            PDLClientError.NavnKunneIkkeAvklares -> {
                LOG.error { "Navn kunne ikke avklares, DETTE SKAL IKKE SKJE" }
                throw IllegalStateException("Navn kunne ikke avklares")
            }

            PDLClientError.FødselKunneIkkeAvklares -> {
                LOG.error { "Fødsel kunne ikke avklares, DETTE SKAL IKKE SKJE" }
                throw IllegalStateException("Fødsel kunne ikke avklares")
            }

            PDLClientError.AdressebeskyttelseKunneIkkeAvklares -> {
                LOG.error { "Adressebeskyttelse kunne ikke avklares, DETTE SKAL IKKE SKJE" }
                throw IllegalStateException("Adressebeskyttelse kunne ikke avklares")
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
                return PersonRespons(feil = Feilmelding.PersonIkkeFunnet)
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

    private fun loggVedInngang(packet: JsonMessage) {
        LOG.info(
            "løser person-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText())
        )
        SECURELOG.info(
            "løser person-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText())
        )
        SECURELOG.debug { "mottok melding: ${packet.toJson()}" }
    }

    private fun loggVedUtgang(packet: JsonMessage) {
        LOG.info(
            "har løst person-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText())
        )
        SECURELOG.info(
            "har løst person-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText())
        )
        SECURELOG.debug { "publiserer melding: ${packet.toJson()}" }
    }

    private fun loggVedFeil(ex: Throwable, packet: JsonMessage) {
        LOG.error(
            "feil ved behandling av behov med id {} og behovId {}, se securelogs for detaljer",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        SECURELOG.error(
            "feil ${ex.message} ved behandling av behov med id {} og behovId {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
            ex
        )
    }
}
