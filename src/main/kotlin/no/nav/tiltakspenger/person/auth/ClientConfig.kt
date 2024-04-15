package no.nav.tiltakspenger.person.auth

import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod
import io.ktor.client.HttpClient
import io.ktor.server.config.ApplicationConfig
import no.nav.security.token.support.client.core.ClientAuthenticationProperties

class ClientConfig(
    applicationConfig: ApplicationConfig,
    httpClient: HttpClient,
) {

    internal val clients: Map<String, OAuth2Client> =
        applicationConfig.configList(CLIENTS_PATH)
            .associate { clientConfig ->
                val wellKnownUrl = clientConfig.propertyToString("well_known_url")
                val clientAuth = ClientAuthenticationProperties(
                    clientConfig.propertyToString("authentication.client_id"),
                    ClientAuthenticationMethod(
                        clientConfig.propertyToString("authentication.client_auth_method"),
                    ),
                    clientConfig.propertyToStringOrNull("authentication.client_secret"),
                    clientConfig.propertyToStringOrNull("authentication.client_jwk"),
                )
                val cacheEnabled = clientConfig.propertyToString("cache_enabled").toBoolean()
                clientConfig.propertyToString(CLIENT_NAME) to OAuth2Client(
                    httpClient = httpClient,
                    wellKnownUrl = wellKnownUrl,
                    clientAuthProperties = clientAuth,
                    cacheConfig = OAuth2CacheConfig(cacheEnabled),
                )
            }

    companion object CommonConfigurationAttributes {
        const val COMMON_PREFIX = "no.nav.security.jwt.client.registration"
        const val CLIENTS_PATH = "$COMMON_PREFIX.clients"
        const val CLIENT_NAME = "client_name"
    }
}

internal fun ApplicationConfig.propertyToString(prop: String) = this.property(prop).getString()
internal fun ApplicationConfig.propertyToStringOrNull(prop: String) = this.propertyOrNull(prop)?.getString()
