package no.nav.tiltakspenger.person.domain.models

import no.nav.tiltakspenger.person.Person

class Respons(
    val person: Person? = null,
    val feil: Feilmelding? = null
)
