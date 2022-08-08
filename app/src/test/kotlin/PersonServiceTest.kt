
import arrow.core.right
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.fakta.person.PersonService
import no.nav.tiltakspenger.fakta.person.domain.models.Barn
import no.nav.tiltakspenger.fakta.person.domain.models.Person
import no.nav.tiltakspenger.fakta.person.pdl.PDLClient
import no.nav.tiltakspenger.fakta.person.pdl.models.AdressebeskyttelseGradering
import no.nav.tiltakspenger.fakta.person.pdl.query
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate
import java.time.Month

class PersonServiceTest {
    private fun mockRapid(): Pair<TestRapid, PDLClient> {
        val person = Person(
            barn = listOf(
                Barn(
                    fornavn = "test",
                    etternavn = "testesen",
                    mellomnavn = null,
                    fødselsdato = LocalDate.of(2022, Month.JUNE, 21)
                )
            ),
            fødselsdato = LocalDate.of(2020, Month.APRIL, 10),
            fornavn = "test",
            mellomnavn = null,
            etternavn = "testesen",
            adressebeskyttelseGradering = AdressebeskyttelseGradering.UGRADERT,
            gtLand = "NOR",
            gtKommune = null,
            gtBydel = null,
        )

        val rapid = TestRapid()
        val pdlClient = mockk<PDLClient>()
        PersonService(
            rapidsConnection = rapid,
            pdlClient = pdlClient
        )
        coEvery { pdlClient.hentPerson(any()) } returns person.right()
        return Pair(rapid, pdlClient)
    }

    @Test
    fun `skal svare på person-behov`() {
        val (rapid, pdlClient) = mockRapid()
        val ident = "121212132323"
        // language=JSON
        rapid.sendTestMessage(
            """
            { 
              "@behov": ["Persondata"], 
              "identer": [{"id":"$ident","type":"fnr","historisk":false}], 
              "@id": "1", 
              "@behovId": "2"
            }
            """.trimIndent()
        )
        coVerify { pdlClient.hentPerson(ident) }

        // language=JSON
        JSONAssert.assertEquals(
            """
            {"@løsning": {
                "person": {
                    "fornavn": "test",
                    "etternavn":  "testesen",
                    "mellomnavn": null,
                    "fødselsdato": "2020-04-10",
                    "adressebeskyttelseGradering": "UGRADERT",
                    "gtLand": "NOR",
                    "gtKommune": null,
                    "gtBydel": null,
                    "barn": [{
                      "fornavn": "test",
                      "etternavn": "testesen",
                      "mellomnavn": null,
                      "fødselsdato": "2022-06-21"
                    }]
                },
                "feil": null
              }
            }
            """.trimIndent(),
            rapid.inspektør.message(0).toString(), JSONCompareMode.LENIENT
        )
    }

    @Test
    fun `should be able to read query-file`() {
        assertTrue(query.isNotEmpty())
    }

    @Test
    fun `skal ikke svare på person-behov som er løst`() {
        val (rapid, pdlClient) = mockRapid()
        val ident = "121212132323"
        // language=JSON
        rapid.sendTestMessage(
            """
            { 
                "@behov": ["person"], 
                "identer": [{"id":"$ident","type":"fnr","historisk":false}], 
                "@id": "1", 
                "@behovId": "2", 
                "@løsning": "hei"
            }
            """.trimIndent()
        )
        coVerify { pdlClient.hentPerson(any()) wasNot Called }
    }
}
