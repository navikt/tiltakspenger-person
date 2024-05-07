package no.nav.tiltakspenger.person.auth

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.tiltakspenger.person.pdl.PDLClient

object Configuration {

    private val otherDefaultProperties = mapOf(
        "application.httpPort" to 8080.toString(),
        "AZURE_APP_CLIENT_ID" to System.getenv("AZURE_APP_CLIENT_ID"),
        "AZURE_APP_CLIENT_SECRET" to System.getenv("AZURE_APP_CLIENT_SECRET"),
        "AZURE_APP_WELL_KNOWN_URL" to System.getenv("AZURE_APP_WELL_KNOWN_URL"),
        "logback.configurationFile" to "logback.xml",
    )
    private val defaultProperties = ConfigurationMap(otherDefaultProperties)

    private val localProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.LOCAL.toString(),
            "PDL_SCOPE" to "api://localhost:8091/.default",
            "PDL_ENDPOINT_URL" to "https://localhost:8091/graphql",
            "logback.configurationFile" to "logback.local.xml",
            "AZURE_APP_CLIENT_ID" to "Azure_test_clientid",
            "AZURE_APP_CLIENT_SECRET" to "Azure_test_client_secret",
            "AZURE_APP_WELL_KNOWN_URL" to "http://localhost:8080/default/.well-known/openid-configuration",
            "AZURE_OPENID_CONFIG_ISSUER" to "http://host.docker.internal:6969/azure",
            "AZURE_OPENID_CONFIG_JWKS_URI" to "http://host.docker.internal:6969/azure/jwks",
            "TOKEN_X_CLIENT_ID" to "tokenx_clientid",
            "TOKEN_X_WELL_KNOWN_URL" to "http://localhost:8080/default/.well-known/openid-configuration",
            "TOKEN_X_PRIVATE_JWK" to "{\n" +
                "    \"p\": \"_xCPvqs85ZZVg460Qfot26rQoNRPTOVDo5p4nqH3ep6BK_5TvoU5LFXd26W-1V1Lc5fcvvftClPOT201xgat4DVtliNtoc8od_tWr190A3AzbsAVFOx0nKa5uhLBxP9SsPM84llp6PXF6QTMGFiPYuoLDaQQqL1K4BbHq3ZzF2M\",\n" +
                "    \"kty\": \"RSA\",\n" +
                "    \"q\": \"7QLqW75zkfSDrn5rMoF50WXyB_ysNx6-2SvaXKGXaOn80IR7QW5vwkleJnsdz_1kr04rJws2p4HBJjUFfSJDi1Dapj7tbIwb0a1szDs6Y2fAa3DlzgXZCkoE2TIrW6UITgs14pI_a7RasclE71FpoZ78XNBvj3NmZugkNLBvRjs\",\n" +
                "    \"d\": \"f7aT4poed8uKdcSD95mvbfBdb6X-M86d99su0c390d6gWwYudeilDugH9PMwqUeUhY0tdaRVXr6rDDIKLSE-uEyaYKaramev0cG-J_QWYJU2Lx-4vDGNHAE7gC99o1Ee_LXqMDCBawMYyVcSWx7PxGQfzhSsARsAIbkarO1sg9zsqPS4exSMbK8wyCTPgRbnkB32_UdZSGbdSib1jSYyyoAItZ8oZHiltVsZIlA97kS4AGPtozde043NC7Ik0uEzgB5qJ_tR7vW8MfDrBj6da2NrLh0UH-q28dooBO1vEu0rvKZIescXYk9lk1ZakHhhpZaLykDOGzxCpronzP3_kQ\",\n" +
                "    \"e\": \"AQAB\",\n" +
                "    \"use\": \"sig\",\n" +
                "    \"qi\": \"9kMIR6pEoiwN3M6O0n8bnh6c3KbLMoQQ1j8_Zyir7ZIlmRpWYl6HtK0VnD88zUuNKTrQa7-jfE5uAUa0PubzfRqybACb4S3HIAuSQP00_yCPzCSRrbpGRDFqq-8eWVwI9VdiN4oqkaaWcL1pd54IDcHIbfk-ZtNtZgsOlodeRMo\",\n" +
                "    \"dp\": \"VUecSAvI2JpjDRFxg326R2_dQWi6-uLMsq67FY7hx8WnOqZWKaUxcHllLENGguAmkgd8bv1F6-YJXNUO3Z7uE8DJWyGNTkSNK1CFsy0fBOdGywi-A7jrZFT6VBRhZRRY-YDaInPyzUkfWsGX26wAhPnrqCvqxgBEQJhdOh7obDE\",\n" +
                "    \"alg\": \"RS256\",\n" +
                "    \"dq\": \"7EUfw92T8EhEjUrRKkQQYEK0iGnGdBxePLiOshEUky3PLT8kcBHbr17cUJgjHBiKqofOVNnE3i9nkOMCWcAyfUtY7KmGndL-WIP-FYplpnrjQzgEnuENgEhRlQOCXZWjNcnPKdKJDqF4WAtAgSIznz6SbSQMUoDD8IoyraPFCck\",\n" +
                "    \"n\": \"7CU8tTANiN6W_fD9SP1dK2vQvCkf7-nwvBYe5CfANV0_Bb0ZmQb77FVVsl1beJ7EYLz3cJmL8Is1RCHKUK_4ydqihNjEWTyZiQoj1i67pkqk_zRvfQa9raZR4uZbuBxx7dWUoPC6fFH2F_psAlHW0zf90fsLvhB6Aqq3uvO7XXqo8qNl9d_JSG0Rg_2QUYVb0WKmPVbbhgwtkFu0Tyuev-VZ9IzTbbr5wmZwEUVY7YAi73pDJkcZt5r2WjOF_cuIXe-O2vwbOrRgmJfHO9--mVLdATnEyrb6q2oy_75h6JjP-R4-TD1hyoFFoE2gmj-kSS6Z_Gggljs3Aw7--Nh10Q\"\n" +
                "}",
            "PDL_AUDIENCE" to "dev-fss:pdl:pdl-api",
        ),
    )
    private val devProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.DEV.toString(),
            "PDL_SCOPE" to "api://dev-fss.pdl.pdl-api/.default",
            "PDL_ENDPOINT_URL" to "https://pdl-api.dev-fss-pub.nais.io/graphql",
            "PDL_AUDIENCE" to "dev-fss:pdl:pdl-api",
        ),
    )
    private val prodProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.PROD.toString(),
            "PDL_SCOPE" to "api://prod-fss.pdl.pdl-api/.default",
            "PDL_ENDPOINT_URL" to "https://pdl-api.prod-fss-pub.nais.io/graphql",
            "PDL_AUDIENCE" to "prod-fss:pdl:pdl-api",
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

    fun logbackConfigurationFile() = config()[Key("logback.configurationFile", stringType)]

    fun httpPort() = config()[Key("application.httpPort", intType)]

    fun oauthPDLAzureConfig(
        scope: String = config()[Key("PDL_SCOPE", stringType)],
        clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        wellknownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
    ) = TokenProvider.OauthAzureConfig(
        scope = scope,
        clientId = clientId,
        clientSecret = clientSecret,
        wellknownUrl = wellknownUrl,
    )

    fun oauthPDLTokenxConfig(
        clientId: String = config()[Key("TOKEN_X_CLIENT_ID", stringType)],
        privateKeyJWT: String = config()[Key("TOKEN_X_PRIVATE_JWK", stringType)],
        wellknownUrl: String = config()[Key("TOKEN_X_WELL_KNOWN_URL", stringType)],
        audience: String = config()[Key("PDL_AUDIENCE", stringType)],
    ) = TokenProvider.OauthTokenxConfig(
        clientId = clientId,
        clientJwk = privateKeyJWT,
        wellknownUrl = wellknownUrl,
        audience = audience,
    )

    fun pdlKlientConfig(baseUrl: String = config()[Key("PDL_ENDPOINT_URL", stringType)]) =
        PDLClient.PdlKlientConfig(baseUrl = baseUrl)
}

enum class Profile {
    LOCAL, DEV, PROD
}
