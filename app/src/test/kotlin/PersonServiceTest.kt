
import arrow.core.right
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.fakta.person.PersonService
import no.nav.tiltakspenger.fakta.person.pdl.Endring
import no.nav.tiltakspenger.fakta.person.pdl.EndringsMetadata
import no.nav.tiltakspenger.fakta.person.pdl.PDLClient
import no.nav.tiltakspenger.fakta.person.pdl.models.Adressebeskyttelse
import no.nav.tiltakspenger.fakta.person.pdl.models.AdressebeskyttelseGradering
import no.nav.tiltakspenger.fakta.person.pdl.models.FolkeregisterMetadata
import no.nav.tiltakspenger.fakta.person.pdl.models.Fødsel
import no.nav.tiltakspenger.fakta.person.pdl.models.Kilde
import no.nav.tiltakspenger.fakta.person.pdl.models.Navn
import no.nav.tiltakspenger.fakta.person.pdl.models.Person
import no.nav.tiltakspenger.fakta.person.pdl.query
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

class PersonServiceTest {
    private fun mockRapid(): Triple<TestRapid, PersonService, PDLClient> {
        val metadata = EndringsMetadata(
            endringer = listOf(
                Endring(
                    kilde = Kilde.FREG,
                    type = "OPPRETT",
                    registrert = LocalDateTime.now(),
                    registrertAv = "",
                    systemkilde = Kilde.FREG
                )
            ),
            master = Kilde.FREG
        )
        val folkeregisterMetadata = FolkeregisterMetadata(
            kilde = Kilde.FREG,
            sekvens = null,
            gyldighetstidspunkt = LocalDateTime.now(),
            ajourholdstidspunkt = LocalDateTime.now(),
            aarsak = null,
            opphoerstidspunkt = null
        )
        val person = Person(
            fødsel = Fødsel(
                foedselsdato = LocalDate.of(2020, Month.APRIL, 10),
                folkeregistermetadata = FolkeregisterMetadata(
                    aarsak = null,
                    ajourholdstidspunkt = LocalDateTime.now(),
                    gyldighetstidspunkt = LocalDateTime.now(),
                    kilde = "",
                    opphoerstidspunkt = null,
                    sekvens = 1,
                ),
                metadata = EndringsMetadata(
                    endringer = listOf(),
                    master = Kilde.FREG
                )
            ),
            navn = Navn(
                fornavn = "test",
                etternavn = "testesen",
                metadata = metadata,
                folkeregistermetadata = folkeregisterMetadata
            ),
            adressebeskyttelse = Adressebeskyttelse(
                gradering = AdressebeskyttelseGradering.UGRADERT,
                folkeregistermetadata = FolkeregisterMetadata(
                    aarsak = null,
                    ajourholdstidspunkt = LocalDateTime.now(),
                    gyldighetstidspunkt = LocalDateTime.now(),
                    kilde = "",
                    opphoerstidspunkt = null,
                    sekvens = 1,
                ),
                metadata = EndringsMetadata(
                    endringer = listOf(),
                    master = Kilde.FREG
                )
            )
        )

        val rapid = TestRapid()
        val pdlClient = mockk<PDLClient>()
        val personService = PersonService(
            rapidsConnection = rapid,
            pdlClient = pdlClient
        )
        coEvery { pdlClient.hentPerson(any()) } returns person.right()
        return Triple(rapid, personService, pdlClient)
    }

    @Test
    fun `skal svare på person-behov`() {
        val (rapid, _, pdlClient) = mockRapid()
        val ident = "121212132323"
        // language=JSON
        rapid.sendTestMessage(
            """
            { 
              "@behov": ["person"], 
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
                "fornavn": "test", 
                "etternavn":  "testesen", 
                "mellomnavn": null, 
                "fødselsdato": "2020-04-10",
                "adressebeskyttelseGradering": "UGRADERT"
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
        val (rapid, _, pdlClient) = mockRapid()
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

fun Int.januar(year: Int): LocalDate = LocalDate.of(year, Month.JANUARY, this)
fun Int.februar(year: Int): LocalDate = LocalDate.of(year, Month.FEBRUARY, this)
fun Int.mars(year: Int): LocalDate = LocalDate.of(year, Month.MARCH, this)
fun Int.april(year: Int): LocalDate = LocalDate.of(year, Month.APRIL, this)
fun Int.mai(year: Int): LocalDate = LocalDate.of(year, Month.MAY, this)
fun Int.juni(year: Int): LocalDate = LocalDate.of(year, Month.JUNE, this)
fun Int.juli(year: Int): LocalDate = LocalDate.of(year, Month.JULY, this)
fun Int.august(year: Int): LocalDate = LocalDate.of(year, Month.AUGUST, this)
fun Int.september(year: Int): LocalDate = LocalDate.of(year, Month.SEPTEMBER, this)
fun Int.oktober(year: Int): LocalDate = LocalDate.of(year, Month.OCTOBER, this)
fun Int.november(year: Int): LocalDate = LocalDate.of(year, Month.NOVEMBER, this)
fun Int.desember(year: Int): LocalDate = LocalDate.of(year, Month.DECEMBER, this)
