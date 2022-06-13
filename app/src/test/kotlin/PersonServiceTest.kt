import io.mockk.*
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.fakta.person.PersonService
import no.nav.tiltakspenger.fakta.person.pdl.PDLClient
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class PersonServiceTest {
    val rapid = TestRapid()
    val personService = PersonService(
        rapidsConnection = rapid
    )

    init {
        mockkObject(PDLClient).also {
            coEvery { PDLClient.hentPerson(any()) } returns mockk()
        }
    }

    @Test
    fun `skal svare på person-behov`() {
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
        coVerify { PDLClient.hentPerson(ident) }

        // language=JSON
        JSONAssert.assertEquals("""
            {"@løsning": {"person": {"navn": "Kåre Kropp"} } }
        """.trimIndent(), rapid.inspektør.message(0).toString(), JSONCompareMode.LENIENT)
    }

    @Test
    fun `skal ikke svare på person-behov som er løst`() {
        val ident = "121212132323"
        // language=JSON
        rapid.sendTestMessage("""
            { "@behov": ["person"], "identer": [{"id":"$ident","type":"fnr","historisk":false}], "@id": "1", "@behovId": "2", "@løsning": "hei" }
        """.trimIndent())
        coVerify { PDLClient.hentPerson(any()) wasNot Called }
    }
}