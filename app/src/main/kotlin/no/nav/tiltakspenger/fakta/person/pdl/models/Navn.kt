package no.nav.tiltakspenger.fakta.person.pdl.models;

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.pdl.EndringsMetadata
import no.nav.tiltakspenger.fakta.person.pdl.PDLClientError
import no.nav.tiltakspenger.fakta.person.pdl.nyeste

@Serializable
data class Navn(
    val fornavn: String,
    val etternavn: String,
    val mellomnavn: String? = null,
    override val metadata: EndringsMetadata,
    override val folkeregistermetadata: FolkeregisterMetadata
): Changeable

fun avklarNavn(navn: List<Navn>): Either<PDLClientError.NavnKunneIkkeAvklares, Navn> {
    if (navn.isEmpty()) return PDLClientError.NavnKunneIkkeAvklares.left()
    return navn
        .sortedByDescending { getEndringstidspunktOrNull(it) }
        .firstOrNull()
        ?.let {
            if (kildeErUdokumentert(it.metadata)) it.right()
            else null
        } ?: PDLClientError.NavnKunneIkkeAvklares.left()
}

fun kildeErUdokumentert(metadata: EndringsMetadata) =
    metadata.master == Kilde.PDL && metadata.endringer.nyeste()?.kilde == Kilde.BRUKER_SELV