package no.nav.tiltakspenger.azureAuth

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun azureClient(config: OauthConfig, engine: HttpClientEngine = CIO.create()): HttpClient {
    val provider = AzureTokenProvider(config, engine)
    return HttpClient(engine) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(
                        accessToken = provider.getToken(),
                        // Refresh token are used in refreshToken method if client gets 401
                        // Should't need this if token expiry is checked first
                        refreshToken = ""
                    )
                }
            }
        }
    }
}