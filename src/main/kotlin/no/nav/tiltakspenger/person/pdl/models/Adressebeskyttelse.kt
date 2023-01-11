package no.nav.tiltakspenger.person.pdl.models

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.tiltakspenger.person.pdl.PDLClientError

enum class AdressebeskyttelseGradering {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT
}

data class Adressebeskyttelse(
    val gradering: AdressebeskyttelseGradering,
    override val folkeregistermetadata: FolkeregisterMetadata? = null,
    override val metadata: EndringsMetadata,
) : Changeable

fun avklarGradering(gradering: List<Adressebeskyttelse>): Either<PDLClientError, AdressebeskyttelseGradering> {
    return if (gradering.isEmpty()) {
        AdressebeskyttelseGradering.UGRADERT.right()
    } else {
        gradering
            .sortedByDescending { getEndringstidspunktOrNull(it) }
            .firstOrNull { !kildeErUdokumentert(it.metadata) }?.gradering?.right()
            ?: PDLClientError.AdressebeskyttelseKunneIkkeAvklares.left()
    }
}
