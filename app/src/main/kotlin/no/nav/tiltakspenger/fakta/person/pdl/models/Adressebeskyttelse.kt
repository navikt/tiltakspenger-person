package no.nav.tiltakspenger.fakta.person.pdl.models

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.pdl.EndringsMetadata

@Serializable
data class Adressebeskyttelse(
    val gradering: AdressebeskyttelseGradering,
    val folkeregistermetadata: FolkeregisterMetadata? = null,
    val metadata: EndringsMetadata
)

enum class AdressebeskyttelseGradering {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT
}
