package no.nav.tiltakspenger.person.pdl

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.JsonConvertException
import io.ktor.server.config.ApplicationConfig
import no.nav.tiltakspenger.libs.person.BarnIFolkeregisteret
import no.nav.tiltakspenger.libs.person.Person
import no.nav.tiltakspenger.person.auth.AzureTokenProvider.AzureAuthException
import no.nav.tiltakspenger.person.auth.TokenProvider
import no.nav.tiltakspenger.person.auth.token
import no.nav.tiltakspenger.person.httpClientCIO

const val INDIVIDSTONAD = "IND"

sealed class PDLClientError {
    object IngenNavnFunnet : PDLClientError()
    object NavnKunneIkkeAvklares : PDLClientError()
    object FødselKunneIkkeAvklares : PDLClientError()
    object AdressebeskyttelseKunneIkkeAvklares : PDLClientError()
    object GraderingKunneIkkeAvklares : PDLClientError()
    object ResponsManglerPerson : PDLClientError()
    object FantIkkePerson : PDLClientError()
    data class NetworkError(val exception: Throwable) : PDLClientError()
    data class SerializationException(val exception: Throwable) : PDLClientError()
    data class UkjentFeil(val errors: List<PdlError>) : PDLClientError()
    data class AzureAuthFailureException(val exception: Throwable) : PDLClientError()
}

fun Throwable.toPdlClientError() = when (this) {
    is JsonConvertException -> PDLClientError.SerializationException(this)
    is AzureAuthException -> PDLClientError.AzureAuthFailureException(this)
    else -> PDLClientError.NetworkError(this)
}

class PDLClient(
    private val config: ApplicationConfig,
    private val tokenProvider: TokenProvider,
    private val httpClient: HttpClient = httpClientCIO(),
) {
    private val pdlEndpoint = config.property("endpoints.pdl").getString()
    var token: String = ""

    private suspend fun fetchPerson(ident: String, subjectToken: String?): Either<PDLClientError, HentPersonResponse> {
        token = if (!subjectToken.isNullOrEmpty()) {
            tokenProvider.tokenXTokenProvider(subjectToken)
        } else {
            tokenProvider.azureTokenProvider()
        }

        return kotlin.runCatching {
            httpClient.post(pdlEndpoint) {
                accept(ContentType.Application.Json)
                header("Tema", INDIVIDSTONAD)
                header("behandlingsnummer", "B470")
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(hentPersonQuery(ident))
            }.body<HentPersonResponse>()
        }
            .fold(
                { it.right() },
                { it.toPdlClientError().left() },
            )
    }

    suspend fun hentPerson(ident: String, subjectToken: String?): Either<PDLClientError, Pair<Person, List<String>>> {
        return either {
            val response = fetchPerson(ident, subjectToken).bind()
            response.toPerson().bind()
        }
    }

    suspend fun hentPersonSomBarn(ident: String): Either<PDLClientError, BarnIFolkeregisteret> {
        return either {
            val response = fetchPerson(ident, null).bind()
            response.toBarn(ident).bind()
        }
    }

    data class PdlKlientConfig(
        val baseUrl: String,
    )
}
