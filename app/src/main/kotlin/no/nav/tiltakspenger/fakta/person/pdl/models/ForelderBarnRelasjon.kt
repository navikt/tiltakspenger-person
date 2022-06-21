package no.nav.tiltakspenger.fakta.person.pdl.models

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.domain.models.Barn
import no.nav.tiltakspenger.fakta.person.pdl.EndringsMetadata
import no.nav.tiltakspenger.fakta.person.seralizers.LocalDateSerializer
import java.time.LocalDate
import java.time.LocalDateTime

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
    val relatertPersonsIdent: String,
    val relatertPersonsRolle: ForelderBarnRelasjonRolle,
    val minRolleForPerson: ForelderBarnRelasjonRolle?,
    val relatertPersonUtenFolkeregisteridentifikator: RelatertBiPerson?,
    override val folkeregistermetadata: FolkeregisterMetadata?,
    override val metadata: EndringsMetadata
) : Changeable

fun List<ForelderBarnRelasjon>.toBarn(): List<Barn> {
    return this
        .filter { it.relatertPersonsRolle == ForelderBarnRelasjonRolle.BARN }
        .groupBy { it.relatertPersonsIdent }
        .mapNotNull { (_, barn) ->
            barn.maxByOrNull { getEndringstidspunktOrNull(it) ?: LocalDateTime.MIN }
        }
        .map {
            val barn = it.relatertPersonUtenFolkeregisteridentifikator
            val navn = barn?.navn
            Barn(
                fornavn = navn?.fornavn,
                mellomnavn = navn?.fornavn,
                etternavn = navn?.fornavn,
                fødselsdato = barn?.foedselsdato
            )
        }
}
