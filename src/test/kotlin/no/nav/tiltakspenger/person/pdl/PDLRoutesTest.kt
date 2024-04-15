package no.nav.tiltakspenger.person.pdl
import arrow.core.Either
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering
import no.nav.tiltakspenger.libs.person.BarnIFolkeregisteret
import no.nav.tiltakspenger.libs.person.Person
import no.nav.tiltakspenger.person.configureTestApplication
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class PDLRoutesTest {
    companion object {
        private val mockOAuth2Server = MockOAuth2Server()

        @JvmStatic
        @AfterAll
        fun after(): Unit = mockOAuth2Server.shutdown()

        @JvmStatic
        @BeforeAll
        fun setup(): Unit = mockOAuth2Server.start(8080)
    }

    private val mockedPerson = Person(
        fornavn = "foo",
        etternavn = "bar",
        mellomnavn = "baz",
        fødselsdato = LocalDate.MAX,
        adressebeskyttelseGradering = AdressebeskyttelseGradering.UGRADERT,
        gtLand = "land",
        gtBydel = "bydel",
        gtKommune = "kommune",
        barn = listOf(
            BarnIFolkeregisteret(
                ident = "id",
                fornavn = "fornavn",
                mellomnavn = "mellomnavn",
                etternavn = "etternavn",
                fødselsdato = LocalDate.now(),
                adressebeskyttelseGradering = AdressebeskyttelseGradering.UGRADERT,
            ),
        ),
        barnUtenFolkeregisteridentifikator = emptyList(),
    )

    private val mockedPdlService = mockk<PDLService>().also { mock ->
        coEvery { mock.hentPerson(any(), any()) } returns Either.Right(mockedPerson)
    }

    val testFødselsnummer = "123"
    private fun issueTestToken(
        issuer: String = "tokendings",
        clientId: String = "testClientId",
        claims: Map<String, String> = mapOf(
            "acr" to "idporten-loa-high",
            "pid" to testFødselsnummer,
        ),
    ): SignedJWT {
        return mockOAuth2Server.issueToken(
            issuer,
            clientId,
            DefaultOAuth2TokenCallback(
                audience = listOf("audience"),
                claims = claims,
            ),
        )
    }

    @Test
    fun `pdl-endepunkt til azure skal svare med personalia fra PDLService`() {
        val token = issueTestToken()
        testApplication {
            val client = createClient {
                install(ContentNegotiation) {
                    jackson()
                }
            }

            configureTestApplication(pdlService = mockedPdlService)

            runBlocking {
                client.get("tokenx/pdl/personalia") {
                    contentType(type = ContentType.Application.Json)
                    header("Authorization", "Bearer ${token.serialize()}")
                }
            }
        }
    }
}
