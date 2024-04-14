package no.nav.tiltakspenger.person.auth

import io.ktor.server.config.ApplicationConfig
import no.nav.tiltakspenger.person.httpClientCIO
import no.nav.tiltakspenger.person.httpClientWithRetry
import no.nav.tiltakspenger.soknad.api.auth.oauth.ClientConfig

class TokenProvider(
    private val config: ApplicationConfig,
) {
    suspend fun tokenXTokenProvider(subjectToken: String): String {
        val oauth2ClientTokenX = checkNotNull(ClientConfig(config, httpClientCIO()).clients["tokendings"])
        val pdlAudience = config.property("audience.pdl").getString()
        val tokenResponse = oauth2ClientTokenX.tokenExchange(subjectToken, pdlAudience)
        return tokenResponse.accessToken!!
    }

    suspend fun azureTokenProvider(): String {
        val oauth2CredentialsClient = checkNotNull(ClientConfig(config, httpClientWithRetry()).clients["azure"])
        val pdlScope = config.property("scope.pdl").getString()
        val tokenResponse = oauth2CredentialsClient.clientCredentials(pdlScope)
        return tokenResponse.accessToken!!
    }
}
