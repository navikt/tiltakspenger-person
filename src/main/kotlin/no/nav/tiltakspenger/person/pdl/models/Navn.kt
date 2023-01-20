package no.nav.tiltakspenger.person.pdl.models

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.tiltakspenger.person.pdl.PDLClientError

data class Navn(
    val fornavn: String,
    val etternavn: String,
    val mellomnavn: String? = null,
    override val metadata: EndringsMetadata,
    override val folkeregistermetadata: FolkeregisterMetadata
) : Changeable

fun avklarNavn(navn: List<Navn>): Either<PDLClientError, Navn> {
    if (navn.isEmpty()) return PDLClientError.IngenNavnFunnet.left()
    return navn
        .sortedByDescending { getEndringstidspunktOrNull(it) }
        .firstOrNull { !kildeErUdokumentert(it.metadata) }?.right()
        ?: PDLClientError.NavnKunneIkkeAvklares.left()
}

fun kildeErUdokumentert(metadata: EndringsMetadata) =
    metadata.master == Kilde.PDL && metadata.endringer.nyeste()?.kilde == Kilde.BRUKER_SELV
