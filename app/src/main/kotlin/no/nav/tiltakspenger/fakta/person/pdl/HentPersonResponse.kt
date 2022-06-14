package no.nav.tiltakspenger.fakta.person.pdl

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.pdl.models.Person

@Serializable
data class HentPersonRepsonse(
    val hentPerson: Person?
)

@Serializable
data class HentPersonResponse(
    val data: HentPersonRepsonse? = null,
    val errors: List<PdlError>? = null
)
