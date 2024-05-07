package no.nav.tiltakspenger.person.pdl

import arrow.core.getOrElse
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.fail
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.tiltakspenger.libs.person.Person
import no.nav.tiltakspenger.person.auth.Configuration
import no.nav.tiltakspenger.person.auth.TokenProvider
import no.nav.tiltakspenger.person.httpClientGeneric
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PDLClientTest {

    val mockTokenProvider = mockk<TokenProvider>(relaxed = true)
    private fun getPDLClient(response: String?): PDLClient {
        return PDLClient(
            pdlKlientConfig = Configuration.pdlKlientConfig(),
            tokenProvider = mockTokenProvider,
            httpClient = httpClientGeneric(mockEngine(response!!)),
        )
    }

    private fun mockEngine(response: String): MockEngine {
        return MockEngine {
            respond(
                content = response,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
    }

    @Test
    fun `should be able to serialize non-errors`() {
        val response = this::class.java.getResource("/pdlResponse.json")?.readText()
        val pdlClient = getPDLClient(response.toString())

        runBlocking {
            pdlClient.hentPersonMedTokenx("test", "subjectToken")
        }.shouldBeRight()
    }

    @Test
    fun `serialisering av barn med manglende ident`() {
        val response = this::class.java.getResource("/pdlResponseManglendeIdentPåBarn.json")?.readText()
        val pdlClient = getPDLClient(response)

        runBlocking {
            val pair = pdlClient.hentPersonMedAzure("test").getOrElse { }
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
        val response = this::class.java.getResource("/pdlErrorResponse.json")?.readText()
        val pdlClient = getPDLClient(response)

        runBlocking {
            pdlClient.hentPersonMedAzure("test")
        }.shouldBeLeft()
    }

    @Test
    fun `should handle `() {
        val response = """{ "lol": "lal" }"""
        val pdlClient = getPDLClient(response)
        runBlocking {
            pdlClient.hentPersonMedAzure("test")
        }
            .mapLeft { it shouldBeSameInstanceAs PDLClientError.ResponsManglerPerson }
            .map { fail("Serialization of bad payload should result in an error") }
    }

    @Test
    fun `should map invalid json to serialization`() {
        val response = """asd{ "lol": "lal" }"""
        val pdlClient = getPDLClient(response)

        runBlocking {
            pdlClient.hentPersonMedAzure("test")
        }
            .mapLeft { (it is PDLClientError.SerializationException) shouldBe true }
            .map { fail("Serialization of bad payload should result in an error") }
    }

    @Test
    fun `should handle navn with null in folkeregisterdata`() {
        val response = this::class.java.getResource("/pdlResponseManglerFolkeregisterdata.json")?.readText()
        val pdlClient = getPDLClient(response)

        runBlocking {
            pdlClient.hentPersonMedAzure("test")
        }.shouldBeRight()
    }
}
