package no.nav.tiltakspenger.fakta.person.pdl.models

import kotlinx.serialization.Serializable

@Serializable
enum class GtType {
    KOMMUNE,
    BYDEL,
    UTLAND,
    UDEFINERT
}

@Serializable
data class GeografiskTilknytning(
    val gtType: GtType,
    val gtKommune: String?,
    val gtBydel: String?,
    val gtLand: String?,
    val regel: String
)
