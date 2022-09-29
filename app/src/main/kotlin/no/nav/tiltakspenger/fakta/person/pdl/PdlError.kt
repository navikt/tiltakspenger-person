package no.nav.tiltakspenger.fakta.person.pdl


data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>? = null,
    val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
    val line: Int?,
    val column: Int?
)

data class PdlErrorExtension(
    val code: String? = null,
    val classification: String
)
