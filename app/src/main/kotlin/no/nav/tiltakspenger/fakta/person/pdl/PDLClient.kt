package no.nav.tiltakspenger.fakta.person.pdl

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.SerializationException
import no.nav.tiltakspenger.azureAuth.OauthConfig
import no.nav.tiltakspenger.azureAuth.azureClient
import no.nav.tiltakspenger.fakta.person.Configuration
import no.nav.tiltakspenger.fakta.person.Configuration.getPDLUrl
import no.nav.tiltakspenger.fakta.person.pdl.models.Person
import no.nav.tiltakspenger.fakta.person.pdl.models.avklarFødsel
import no.nav.tiltakspenger.fakta.person.pdl.models.avklarGradering
import no.nav.tiltakspenger.fakta.person.pdl.models.avklarNavn

val url = getPDLUrl()
const val INDIVIDSTONAD = "IND"

sealed class PDLClientError {
    object IngenNavnFunnet : PDLClientError()
    object NavnKunneIkkeAvklares : PDLClientError()
    object GraderingKunneIkkeAvklares : PDLClientError()
    object ResponsManglerPerson : PDLClientError()
    data class NetworkError(val exception: Throwable) : PDLClientError()
    data class SerializationException(val exception: Throwable) : PDLClientError()
    data class UkjentFeil(val errors: List<PdlError>) : PDLClientError()
}

fun Throwable.toPdlClientError() = when (this) {
    is SerializationException -> PDLClientError.SerializationException(this)
    else -> PDLClientError.NetworkError(this)
}

class PDLClient(
    private val client: HttpClient = azureClient(
        OauthConfig.fromEnv(
            scope = Configuration.getPdlScope(),
        ),
    ) {
        install(HttpRequestRetry) {
            retryOnExceptionIf { _, cause ->
                cause is IOException
            }
            exponentialDelay()
        }
    },
) {
    private suspend fun fetchPerson(ident: String): Either<PDLClientError, HentPersonResponse> {
        return kotlin.runCatching {
            client.post(url) {
                accept(ContentType.Application.Json)
                header("Tema", INDIVIDSTONAD)
                contentType(ContentType.Application.Json)
                setBody(hentPersonQuery(ident))
            }.body<HentPersonResponse>()
        }
            .fold(
                { it.right() },
                { it.toPdlClientError().left() },
            )
    }

    suspend fun hentPerson(ident: String): Either<PDLClientError, Person> {
        return either {
            val response = fetchPerson(ident).bind()
            val person = response.extractPerson().bind()
            Person(
                fødsel = avklarFødsel(person.foedsel),
                navn = avklarNavn(person.navn).bind(),
                adressebeskyttelse = avklarGradering(person.adressebeskyttelse)
            )
        }
    }
}
