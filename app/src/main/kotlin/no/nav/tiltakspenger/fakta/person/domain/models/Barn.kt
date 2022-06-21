package no.nav.tiltakspenger.fakta.person.domain.models

import java.time.LocalDate

data class Barn(
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val fødselsdato: LocalDate?
)
