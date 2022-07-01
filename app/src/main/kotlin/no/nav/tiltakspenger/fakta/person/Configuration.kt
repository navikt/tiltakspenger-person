package no.nav.tiltakspenger.fakta.person

internal object Configuration {
    private fun getPropertyValueByEnvironment(devValue: String, prodValue: String): String {
        return when (System.getenv("NAIS_CLUSTER_NAME")) {
            "dev-gcp" -> devValue
            "prod-gcp" -> prodValue
            else -> devValue
        }
    }

    fun getPDLUrl(): String = getPropertyValueByEnvironment(
        devValue = "https://pdl-api.dev-fss-pub.nais.io/graphql",
        prodValue = "https://pdl-api.prod-fss-pub.nais.io/graphql "
    )
    fun getPdlScope(): String = getPropertyValueByEnvironment(
        devValue = "api://dev-fss.pdl.pdl-api/.default",
        prodValue = "api://prod-fss.pdl.pdl-api/.default"
    )

    private val defaultProperties =
        mapOf(
            "RAPID_APP_NAME" to "tiltakspenger-fakta-person",
            "KAFKA_BROKERS" to System.getenv("KAFKA_BROKERS"),
            "KAFKA_CREDSTORE_PASSWORD" to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
            "KAFKA_TRUSTSTORE_PATH" to System.getenv("KAFKA_TRUSTSTORE_PATH"),
            "KAFKA_KEYSTORE_PATH" to System.getenv("KAFKA_KEYSTORE_PATH"),
            "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
            "KAFKA_RESET_POLICY" to "latest",
            "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-fakta-person-v1"
        )

    fun asMap(): Map<String, String> = defaultProperties
}
