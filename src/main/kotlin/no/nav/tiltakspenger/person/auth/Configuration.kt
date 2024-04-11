package no.nav.tiltakspenger.person.auth

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.tiltakspenger.person.pdl.PDLClient

internal object Configuration {

    val rapidsAndRivers = mapOf(
        "RAPID_APP_NAME" to "tiltakspenger-person",
        "KAFKA_BROKERS" to System.getenv("KAFKA_BROKERS"),
        "KAFKA_CREDSTORE_PASSWORD" to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
        "KAFKA_TRUSTSTORE_PATH" to System.getenv("KAFKA_TRUSTSTORE_PATH"),
        "KAFKA_KEYSTORE_PATH" to System.getenv("KAFKA_KEYSTORE_PATH"),
        "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
        "KAFKA_RESET_POLICY" to "latest",
        "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-person-v1",
    )

    private val otherDefaultProperties = mapOf(
        "application.httpPort" to 8080.toString(),
        "AZURE_APP_CLIENT_ID" to System.getenv("AZURE_APP_CLIENT_ID"),
        "AZURE_APP_CLIENT_SECRET" to System.getenv("AZURE_APP_CLIENT_SECRET"),
        "AZURE_APP_WELL_KNOWN_URL" to System.getenv("AZURE_APP_WELL_KNOWN_URL"),
    )
    private val defaultProperties = ConfigurationMap(rapidsAndRivers + otherDefaultProperties)

    private val localProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.LOCAL.toString(),
            "pdlScope" to "api://dev-fss.pdl.pdl-api/.default",
            "pdlBaseUrl" to "https://pdl-api.dev-fss-pub.nais.io/graphql",
        ),
    )
    private val devProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.DEV.toString(),
            "pdlScope" to "api://dev-fss.pdl.pdl-api/.default",
            "pdlBaseUrl" to "https://pdl-api.dev-fss-pub.nais.io/graphql",
        ),
    )
    private val prodProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.PROD.toString(),
            "pdlScope" to "api://prod-fss.pdl.pdl-api/.default",
            "pdlBaseUrl" to "https://pdl-api.prod-fss-pub.nais.io/graphql",
        ),
    )

    private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-gcp" ->
            systemProperties() overriding EnvironmentVariables overriding devProperties overriding defaultProperties

        "prod-gcp" ->
            systemProperties() overriding EnvironmentVariables overriding prodProperties overriding defaultProperties

        else -> {
            systemProperties() overriding EnvironmentVariables overriding localProperties overriding defaultProperties
        }
    }

    fun httpPort() = config()[Key("application.httpPort", intType)]

    // tokendings(tokenX) auth for å authentisere ekstern sluttbruker
   /* fun tokenxValidationConfig(
        clientId: String = config()[Key("TOKEN_X_CLIENT_ID", stringType)],
        wellKnownUrl: String = config()[Key("TOKEN_X_WELL_KNOWN_URL", stringType)],
        issuer: String = config()[Key("TOKEN_X_ISSUER", stringType)],
        jwksUri: String = config()[Key("TOKEN_X_JWKS_URI", stringType)],
    ) = TokenValidationConfig(
        clientId = clientId,
        wellKnownUrl = wellKnownUrl,
        issuer = issuer,
        jwksUri = jwksUri,
    )

    //azure auth for å authentisere saksbehandler eller systembruker
    fun azureValidationConfig(
        clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        wellKnownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
        issuer: String = config()[Key("AZURE_OPENID_CONFIG_ISSUER", stringType)],
        jwksUri: String = config()[Key("AZURE_OPENID_CONFIG_JWKS_URI", stringType)],
    ) = TokenValidationConfig(
        clientId = clientId,
        wellKnownUrl = wellKnownUrl,
        issuer = issuer,
        jwksUri = jwksUri,
    )

    data class TokenValidationConfig(
        val clientId: String,
        val wellKnownUrl: String,
        val issuer: String,
        val jwksUri: String,
    ) */

    fun oauthPDLAzureConfig(
        scope: String = config()[Key("pdlScope", stringType)],
        clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        wellknownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
    ) = AzureTokenProvider.OauthConfig(
        scope = scope,
        clientId = clientId,
        clientSecret = clientSecret,
        wellknownUrl = wellknownUrl,
    )

    fun pdlKlientConfig(baseUrl: String = config()[Key("pdlBaseUrl", stringType)]) =
        PDLClient.PdlKlientConfig(baseUrl = baseUrl)
}

enum class Profile {
    LOCAL, DEV, PROD
}
