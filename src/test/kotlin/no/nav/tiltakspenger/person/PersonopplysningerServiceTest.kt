package no.nav.tiltakspenger.person

import arrow.core.right
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering
import no.nav.tiltakspenger.libs.person.BarnIFolkeregisteret
import no.nav.tiltakspenger.libs.person.Person
import no.nav.tiltakspenger.person.pdl.PDLService
import no.nav.tiltakspenger.person.pdl.query
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate
import java.time.Month

class PersonopplysningerServiceTest {
    private fun mockRapid(): Pair<TestRapid, PDLService> {
        val person = Person(
            barn = listOf(
                BarnIFolkeregisteret(
                    ident = "ident",
                    fornavn = "test",
                    etternavn = "testesen",
                    mellomnavn = null,
                    fødselsdato = LocalDate.of(2022, Month.JUNE, 21),
                    adressebeskyttelseGradering = AdressebeskyttelseGradering.UGRADERT,
                ),
            ),
            barnUtenFolkeregisteridentifikator = emptyList(),
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
        val pdlService = mockk<PDLService>()
        PersonopplysningerService(
            rapidsConnection = rapid,
            pdlService = pdlService,
        )
        coEvery { pdlService.hentPerson(any()) } returns person.right()
        return Pair(rapid, pdlService)
    }

    @Test
    fun `skal svare på person-behov`() {
        val (rapid, pdlClient) = mockRapid()
        val ident = "121212132323"
        // language=JSON
        rapid.sendTestMessage(
            """
            { 
              "@behov": ["personopplysninger"], 
              "ident": "$ident", 
              "@id": "1", 
              "@behovId": "2"
            }
            """.trimIndent(),
        )
        coVerify { pdlClient.hentPerson(ident) }

        println(rapid.inspektør.message(0).toString())
        // language=JSON
        JSONAssert.assertEquals(
            """
            {"@løsning": {
              "personopplysninger": {
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
            }
            """.trimIndent(),
            rapid.inspektør.message(0).toString(),
            JSONCompareMode.LENIENT,
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
                "@behov": ["personopplysninger"], 
                "ident": "$ident", 
                "@id": "1", 
                "@behovId": "2", 
                "@løsning": "hei"
            }
            """.trimIndent(),
        )
        coVerify { pdlClient.hentPerson(any()) wasNot Called }
    }
}
