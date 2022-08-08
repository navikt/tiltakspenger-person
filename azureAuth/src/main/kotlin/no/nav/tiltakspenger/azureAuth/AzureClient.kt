package no.nav.tiltakspenger.azureAuth

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun azureClient(
    config: OauthConfig,
    engine: HttpClientEngine = CIO.create(),
    configBlock: HttpClientConfig<*>.() -> Unit = {},
): HttpClient {
    val provider = AzureTokenProvider(config, engine)
    return HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(
                        accessToken = provider.getToken(),
                        // Refresh token are used in refreshToken method if client gets 401
                        // Should't need this if token expiry is checked first
                        refreshToken = "",
                    )
                }
            }
        }
        apply(configBlock)
    }
}
