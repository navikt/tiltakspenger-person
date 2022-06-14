import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.fakta.person.pdl.PDLClient
import org.junit.jupiter.api.Test

class PDLClientTest {

    private fun mockClient(response: String): HttpClient {
        val mockEngine = MockEngine() {
            respond(
                // language=JSON
                content = response,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    }

    @Test
    fun `should be able to serialize non-errors`() {
        // language=JSON
        val response = this::class.java.getResource("pdlResponse.json").readText()
        val pdlClient = PDLClient(mockClient(response))

        runBlocking {
            pdlClient.hentPerson("test")
        }
    }

    @Test
    fun `should be able to serialize errors`() {
        // language=JSON
        val response = this::class.java.getResource("pdlErrorResponse.json").readText()
        val pdlClient = PDLClient(mockClient(response))

        runBlocking {
            pdlClient.hentPerson("test")
        }
    }
}