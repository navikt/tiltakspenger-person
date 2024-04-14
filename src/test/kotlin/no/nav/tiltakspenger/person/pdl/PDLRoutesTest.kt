package no.nav.tiltakspenger.person.pdl
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import io.ktor.server.testing.testApplication
import io.mockk.mockk
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering
import no.nav.tiltakspenger.libs.person.BarnIFolkeregisteret
import no.nav.tiltakspenger.libs.person.Person
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

    private val mockedPdlService = mockk<PDLService>(relaxed = true)

    @Test
    fun `personalia-endepunkt til azure skal svare med personalia fra PDLService`() {
        // val token = issueTestToken()
        testApplication {
            val client = createClient {
                install(ContentNegotiation) {
                    jackson()
                }
            }
        }
    }
}
