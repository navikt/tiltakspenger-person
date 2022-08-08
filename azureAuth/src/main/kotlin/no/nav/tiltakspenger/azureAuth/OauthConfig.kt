package no.nav.tiltakspenger.azureAuth

data class OauthConfig(
    val scope: String,
    val clientId: String,
    val clientSecret: String,
    val wellknownUrl: String,
) {
    companion object {
        fun fromEnv(scope: String): OauthConfig {
            return OauthConfig(
                scope = scope,
                clientSecret = System.getenv("AZURE_APP_CLIENT_SECRET"),
                clientId = System.getenv("AZURE_APP_CLIENT_ID"),
                wellknownUrl = System.getenv("AZURE_APP_WELL_KNOWN_URL")
            )
        }
    }
}
