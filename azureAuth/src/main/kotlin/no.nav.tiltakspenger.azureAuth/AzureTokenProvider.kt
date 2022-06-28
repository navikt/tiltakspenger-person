package no.nav.tiltakspenger.azureAuth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AzureTokenProvider(
    private val config: OauthConfig,
    engine: HttpClientEngine
) {
    private val azureHttpClient = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private val wellknown: WellKnown by lazy {
        runBlocking { azureHttpClient.get(config.wellknownUrl).body() }
    }
    private val tokenCache = TokenCache()

    suspend fun getToken(): String {
        val currentToken = tokenCache.token
        if (currentToken != null && !tokenCache.isExpired()) return currentToken
        return clientCredentials()
    }

    private suspend fun clientCredentials(): String {
        return azureHttpClient.submitForm(
            url = wellknown.tokenEndpoint,
            formParameters = Parameters.build {
                appendToken(config)
            }
        ).body<OAuth2AccessTokenResponse>().let {
            tokenCache.update(
                it.accessToken,
                it.expiresIn.toLong()
            )
            return@let it.accessToken
        }
    }
}

fun ParametersBuilder.appendToken(config: OauthConfig) {
    append("grant_type", "client_credentials")
    append("client_id", config.clientId)
    append("client_secret", config.clientSecret)
    append("scope", config.scope)
}

@Serializable
data class WellKnown(
    @SerialName("token_endpoint")
    val tokenEndpoint: String
)
