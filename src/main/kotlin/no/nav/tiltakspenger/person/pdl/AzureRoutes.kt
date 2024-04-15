package no.nav.tiltakspenger.person.pdl

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.person.Feilmelding
import no.nav.tiltakspenger.libs.person.PersonRespons
import no.nav.tiltakspenger.person.auth.getFnrForAzureToken

private val LOG = KotlinLogging.logger {}
const val AZURE_PDL_PATH = "/azure/pdl/personalia"

fun Route.AzureRoutes(pdlService: PDLService) {
    get(AZURE_PDL_PATH) {
        LOG.info { "Mottatt forespørsel for å hente personalia data fra PDL" }
        val ident = call.getFnrForAzureToken() ?: throw IllegalStateException("Mangler fødselsnummer")
        val pdlrespons = pdlService.hentPerson(ident, null)

        pdlrespons.fold(
            {
                val feil =
                    when (it) {
                        is PDLClientError.FantIkkePerson -> Feilmelding.PersonIkkeFunnet
                        is PDLClientError.IngenNavnFunnet -> Feilmelding.NavnIkkeFunnet
                        is PDLClientError.FødselKunneIkkeAvklares -> Feilmelding.FødselKunneIkkeAvklares
                        is PDLClientError.AdressebeskyttelseKunneIkkeAvklares -> Feilmelding.AdressebeskyttelseKunneIkkeAvklares
                        is PDLClientError.GraderingKunneIkkeAvklares -> Feilmelding.GraderingKunneIkkeAvklares
                        is PDLClientError.NavnKunneIkkeAvklares -> Feilmelding.NavnKunneIkkeAvklares
                        is PDLClientError.AzureAuthFailureException -> Feilmelding.AzureAuthFailureException
                        is PDLClientError.SerializationException -> Feilmelding.SerializationException
                        is PDLClientError.NetworkError -> Feilmelding.NetworkError
                        is PDLClientError.ResponsManglerPerson -> Feilmelding.ResponsManglerPerson
                        else -> Feilmelding.UkjentFeil
                    }
                call.respond(PersonRespons(person = null, feil = feil))
            },
            {
                call.respond(status = HttpStatusCode.OK, PersonRespons(person = it, feil = null))
            },
        )
    }
}
