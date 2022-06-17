package no.nav.tiltakspenger.fakta.person.pdl.models

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.pdl.EndringsMetadata

@Serializable
data class Adressebeskyttelse(
    val gradering: AdressebeskyttelseGradering,
    override val folkeregistermetadata: FolkeregisterMetadata? = null,
    override val metadata: EndringsMetadata,
) : Changeable

fun avklarGradering(gradering: List<Adressebeskyttelse>): Adressebeskyttelse? {
    return gradering
        .sortedByDescending { getEndringstidspunktOrNull(it) }
        .firstOrNull { !kildeErUdokumentert(it.metadata) }
}

enum class AdressebeskyttelseGradering {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT
}
