package no.nav.tiltakspenger.fakta.person.pdl

import kotlinx.serialization.Serializable

@Serializable
data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>?,
    val extensions: PdlErrorExtension
)

@Serializable
data class PdlErrorLocation(
    val line: Int?,
    val column: Int?
)

@Serializable
data class PdlErrorExtension(
    val code: String?,
    val classification: String
)