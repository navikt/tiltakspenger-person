package no.nav.tiltakspenger.fakta.person.pdl

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.*
import no.nav.tiltakspenger.fakta.person.Configuration
import no.nav.tiltakspenger.fakta.person.auth.AzureTokenProvider.AzureAuthException
import no.nav.tiltakspenger.fakta.person.defaultHttpClient
import no.nav.tiltakspenger.fakta.person.defaultObjectMapper
import no.nav.tiltakspenger.fakta.person.domain.models.BarnIFolkeregisteret
import no.nav.tiltakspenger.fakta.person.domain.models.Person

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
    private val pdlKlientConfig: PdlKlientConfig = Configuration.pdlKlientConfig(),
    private val objectMapper: ObjectMapper = defaultObjectMapper(),
    private val getToken: suspend () -> String,
    engine: HttpClientEngine? = null,
    private val httpClient: HttpClient = defaultHttpClient(
        objectMapper = objectMapper,
        engine = engine
    ) {}
) {
    private suspend fun fetchPerson(ident: String): Either<PDLClientError, HentPersonResponse> {
        return kotlin.runCatching {
            httpClient.post(pdlKlientConfig.baseUrl) {
                accept(ContentType.Application.Json)
                header("Tema", INDIVIDSTONAD)
                bearerAuth(getToken())
                contentType(ContentType.Application.Json)
                setBody(hentPersonQuery(ident))
            }.body<HentPersonResponse>()
        }
            .fold(
                { it.right() },
                { it.toPdlClientError().left() },
            )
    }

    suspend fun hentPerson(ident: String): Either<PDLClientError, Pair<Person, List<String>>> {
        return either {
            val response = fetchPerson(ident).bind()
            response.toPerson().bind()
        }
    }

    suspend fun hentPersonSomBarn(ident: String): Either<PDLClientError, BarnIFolkeregisteret> {
        return either {
            val response = fetchPerson(ident).bind()
            response.toBarn(ident).bind()
        }
    }

    data class PdlKlientConfig(
        val baseUrl: String,
    )
}
