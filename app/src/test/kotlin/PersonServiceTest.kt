import io.mockk.*
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.fakta.person.PersonService
import no.nav.tiltakspenger.fakta.person.pdl.HentPersonResponse
import no.nav.tiltakspenger.fakta.person.pdl.PDLClient
import no.nav.tiltakspenger.fakta.person.pdl.query
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class PersonServiceTest {
    private fun mockRapid(): Triple<TestRapid, PersonService, PDLClient> {
        val rapid = TestRapid()
        val pdlClient = mockk<PDLClient>()
        val personService = PersonService(
            rapidsConnection = rapid,
            pdlClient = pdlClient
        )
        coEvery { pdlClient.hentPerson(any()) } returns HentPersonResponse(
            data = mockk(),
            errors = emptyList()
        )
        return Triple(rapid, personService, pdlClient)
    }

    @Test
    fun `skal svare på person-behov`() {
        val (rapid, _, pdlClient) = mockRapid()
        val ident = "121212132323"
        // language=JSON
        rapid.sendTestMessage("""
            { 
              "@behov": ["person"], 
              "identer": [{"id":"$ident","type":"fnr","historisk":false}], 
              "@id": "1", 
              "@behovId": "2"
            }
        """.trimIndent())
        coVerify { pdlClient.hentPerson(ident) }

        // language=JSON
        JSONAssert.assertEquals("""
            {"@løsning": {"person": {"navn": "Kåre Kropp"} } }
        """.trimIndent(), rapid.inspektør.message(0).toString(), JSONCompareMode.LENIENT)
    }

    @Test
    fun `should be able to read query-file`() {
        assertTrue(query.isNotEmpty())
    }

    @Test
    fun `skal ikke svare på person-behov som er løst`() {
        val (rapid, _, pdlClient) = mockRapid()
        val ident = "121212132323"
        // language=JSON
        rapid.sendTestMessage("""
            { 
                "@behov": ["person"], 
                "identer": [{"id":"$ident","type":"fnr","historisk":false}], 
                "@id": "1", 
                "@behovId": "2", 
                "@løsning": "hei"
            }
        """.trimIndent())
        coVerify { pdlClient.hentPerson(any()) wasNot Called }
    }
}
