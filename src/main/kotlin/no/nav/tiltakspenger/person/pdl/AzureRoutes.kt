package no.nav.tiltakspenger.person.pdl

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.person.Feilmelding
import no.nav.tiltakspenger.libs.person.PersonRespons

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
const val AZURE_PDL_PATH = "/azure/pdl/personalia"

data class RequestBody(
    val ident: String,
)

fun Route.AzureRoutes(pdlService: PDLService) {
    post(AZURE_PDL_PATH) {
        LOG.info { "Mottatt forespørsel for å hente personalia data fra PDL" }
        val ident = call.receive<RequestBody>().ident
        val pdlrespons = pdlService.hentPersonMedAzure(ident)

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
