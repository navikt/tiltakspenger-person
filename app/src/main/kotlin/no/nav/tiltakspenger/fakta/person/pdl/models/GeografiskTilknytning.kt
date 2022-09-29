package no.nav.tiltakspenger.fakta.person.pdl.models


enum class GtType {
    KOMMUNE,
    BYDEL,
    UTLAND,
    UDEFINERT
}

data class GeografiskTilknytning(
    val gtType: GtType,
    val gtKommune: String?,
    val gtBydel: String?,
    val gtLand: String?,
    val regel: String
)
