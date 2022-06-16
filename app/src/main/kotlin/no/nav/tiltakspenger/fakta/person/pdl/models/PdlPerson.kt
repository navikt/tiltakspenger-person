package no.nav.tiltakspenger.fakta.person.pdl.models

import kotlinx.serialization.Serializable

@Serializable
data class PdlPerson(
    val navn: List<Navn>,
    val foedsel: List<Fødsel>,
    val adressebeskyttelse: List<Adressebeskyttelse>
)