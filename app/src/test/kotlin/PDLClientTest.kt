import arrow.core.getOrHandle
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.fail
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.fakta.person.domain.models.Person
import no.nav.tiltakspenger.fakta.person.pdl.PDLClient
import no.nav.tiltakspenger.fakta.person.pdl.PDLClientError
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

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
    fun `serialisering av barn med manglende ident`() {
        val response = File("src/test/resources/pdlResponseManglendeIdentPåBarn.json").readText()
        val pdlClient = PDLClient(mockClient(response))

        runBlocking {
            val pair = pdlClient.hentPerson("test").getOrHandle { }
            pair should beInstanceOf<Pair<Person, List<String>>>()
            val person = (pair as Pair<Person, List<String>>).first
            val barnsIdenter = pair.second
            barnsIdenter.size shouldBe 0
            person.barnUtenFolkeregisteridentifikator.size shouldBe 1
            person.barnUtenFolkeregisteridentifikator.first().fornavn shouldBe "Geometrisk"
            person.barnUtenFolkeregisteridentifikator.first().mellomnavn shouldBe "Sprudlende"
            person.barnUtenFolkeregisteridentifikator.first().etternavn shouldBe "Jakt"
            person.barnUtenFolkeregisteridentifikator.first().fødselsdato shouldBe LocalDate.of(2016, 5, 23)
            person.barnUtenFolkeregisteridentifikator.first().statsborgerskap shouldBe "BHS"
        }
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
