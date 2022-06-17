
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.fakta.person.pdl.PDLClient
import no.nav.tiltakspenger.fakta.person.pdl.PDLClientError
import org.junit.jupiter.api.Test

class PDLClientTest {

    private fun mockClient(response: String): HttpClient {
        val mockEngine = MockEngine {
            respond(
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
        val response = this::class.java.getResource("pdlResponse.json").readText()
        val pdlClient = PDLClient(mockClient(response))

        runBlocking {
            pdlClient.hentPerson("test")
        }.shouldBeRight()
    }

    @Test
    fun `should be able to serialize errors`() {
        val response = this::class.java.getResource("pdlErrorResponse.json").readText()
        val pdlClient = PDLClient(mockClient(response))

        runBlocking {
            pdlClient.hentPerson("test")
        }.shouldBeLeft()
    }

    @Test
    fun `should handle `() {
        val pdlClient = PDLClient(mockClient("""{ "lol": "lal" }"""))

        runBlocking {
            pdlClient.hentPerson("test")
        }
            .mapLeft { it shouldBeSameInstanceAs PDLClientError.ResponsManglerPerson }
            .map { fail("Serialization of bad payload should result in an error") }
    }

    @Test
    fun `should map invalid json to serialization`() {
        val pdlClient = PDLClient(mockClient("""asd{ "lol": "lal" }"""))

        runBlocking {
            pdlClient.hentPerson("test")
        }
            .mapLeft { (it is PDLClientError.SerializationException) shouldBe true }
            .map { fail("Serialization of bad payload should result in an error") }
    }
}
