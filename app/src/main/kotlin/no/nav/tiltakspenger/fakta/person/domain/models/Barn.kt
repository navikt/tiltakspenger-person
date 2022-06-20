package no.nav.tiltakspenger.fakta.person.domain.models

import java.time.LocalDate

data class Barn(
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val foedselsdato: LocalDate?,
) {
    fun toMap(): Map<String, String?> {
        return mapOf(
            "fornavn" to fornavn,
            "mellomnavn" to mellomnavn,
            "etternavn" to etternavn,
            "foedselsdato" to foedselsdato.toString(),
        )
    }
}
