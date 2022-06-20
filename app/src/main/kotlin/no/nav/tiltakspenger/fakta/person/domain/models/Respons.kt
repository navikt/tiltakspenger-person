package no.nav.tiltakspenger.fakta.person.domain.models

class Respons(
    val person: Person? = null,
    val feil: Feilmelding? = null
) {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "person" to person?.toMap(),
            "feil" to feil?.message
        )
}
