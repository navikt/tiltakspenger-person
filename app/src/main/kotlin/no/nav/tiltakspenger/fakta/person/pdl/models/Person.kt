package no.nav.tiltakspenger.fakta.person.pdl.models

import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val navn: List<Navn>,
    val foedsel: List<Fødsel>
)