package no.nav.tiltakspenger.person.pdl.models

import no.nav.tiltakspenger.person.domain.models.BarnUtenFolkeregisteridentifikator
import java.time.LocalDate

enum class ForelderBarnRelasjonRolle {
    BARN,
    MOR,
    FAR,
    MEDMOR
}

data class Personnavn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
)

enum class KjoennType {
    MANN,
    KVINNE,
    UKJENT
}

data class RelatertBiPerson(
    val navn: Personnavn?,
    val foedselsdato: LocalDate?,
    val statsborgerskap: String?,
    val kjoenn: KjoennType?,
)

data class ForelderBarnRelasjon(
    val relatertPersonsIdent: String?,
    val relatertPersonsRolle: ForelderBarnRelasjonRolle,
    val minRolleForPerson: ForelderBarnRelasjonRolle?,
    val relatertPersonUtenFolkeregisteridentifikator: RelatertBiPerson?,
    override val folkeregistermetadata: FolkeregisterMetadata?,
    override val metadata: EndringsMetadata
) : Changeable

fun List<ForelderBarnRelasjon>.toIdenterForBarnIFolkeregisteret(): List<String> {
    return this
        .asSequence()
        .filter { it.relatertPersonsRolle == ForelderBarnRelasjonRolle.BARN }
        .mapNotNull { it.relatertPersonsIdent }
        .distinct()
        .map { it }
        .toList()
}

fun List<ForelderBarnRelasjon>.toBarnUtenforFolkeregisteret(): List<BarnUtenFolkeregisteridentifikator> {
    return this
        .asSequence()
        .filter { it.relatertPersonsRolle == ForelderBarnRelasjonRolle.BARN }
        .filter { it.relatertPersonUtenFolkeregisteridentifikator != null }
        .map {
            val barn = it.relatertPersonUtenFolkeregisteridentifikator!!
            val navn = barn.navn
            BarnUtenFolkeregisteridentifikator(
                fornavn = navn?.fornavn,
                mellomnavn = navn?.mellomnavn,
                etternavn = navn?.etternavn,
                fødselsdato = barn.foedselsdato,
                statsborgerskap = barn.statsborgerskap
            )
        }
        .toList()
}
