package no.nav.tiltakspenger.fakta.person.pdl.models

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.domain.models.BarnUtenFolkeregisteridentifikator
import no.nav.tiltakspenger.fakta.person.serializers.LocalDateSerializer
import java.time.LocalDate

@Serializable
enum class ForelderBarnRelasjonRolle {
    BARN,
    MOR,
    FAR,
    MEDMOR
}

@Serializable
data class Personnavn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
)

@Serializable
enum class KjoennType {
    MANN,
    KVINNE,
    UKJENT
}

@Serializable
data class RelatertBiPerson(
    val navn: Personnavn?,
    @Serializable(with = LocalDateSerializer::class)
    val foedselsdato: LocalDate?,
    val statsborgerskap: String?,
    val kjoenn: KjoennType?,
)

@Serializable
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
