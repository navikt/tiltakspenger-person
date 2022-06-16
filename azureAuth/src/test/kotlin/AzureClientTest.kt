
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import no.nav.tiltakspenger.azureAuth.OauthConfig
import no.nav.tiltakspenger.azureAuth.azureClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AzureClientTest {

    @Test
    fun `should send requests with provided access token`() {
        val wellKnownUrl = "https://localhost/wellwellwell"
        val tokenEndpoint = "http://localhost/token"
        val accessToken = "abc"
        var actualAuthHeader: String? = null
        val mockEngine = MockEngine { request ->
            when (request.url.toString()) {
                wellKnownUrl -> respond(
                    content = """{ "token_endpoint": "$tokenEndpoint" }""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
                tokenEndpoint -> respond(
                    content = """{ 
                        |"access_token": "$accessToken", 
                        |"token_type": "access_token", 
                        |"ext_expires_in": "1", 
                        |"expires_in": "1" }
                    """.trimMargin(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
                else -> {
                    actualAuthHeader = request.headers.get("Authorization")
                    respond(
                        content = """{ "token_endpoint": "http://localhost/well-known" }""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
        }

        val client = azureClient(
            OauthConfig(
                wellknownUrl = wellKnownUrl,
                clientSecret = "opensecret",
                clientId = "id",
                scope = "scope"
            ),
            mockEngine
        )
        runBlocking {
            client.get("http://localhost:8080/test")
        }
        assertEquals(actualAuthHeader, "Bearer $accessToken")
    }
}
