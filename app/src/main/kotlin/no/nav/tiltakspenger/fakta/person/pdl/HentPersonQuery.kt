package no.nav.tiltakspenger.fakta.person.pdl

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.pdl.models.Fødsel
import no.nav.tiltakspenger.fakta.person.pdl.models.Navn

val query = PDLClient::class.java.getResource("/hentPersonQuery.graphql").readText()

fun hentPersonQuery(ident: String): GraphqlQuery {
    return GraphqlQuery(
        query = query,
        variables = mapOf(
            "ident" to ident
        )
    )
}

@Serializable
data class GraphqlQuery(
    val query: String,
    val variables: Map<String, String>
)

@Serializable
data class Person(
    val navn: List<Navn>,
    val foedsel: List<Fødsel>
)

@Serializable
data class HentPersonRepsonse(
    val hentPerson: Person
)

@Serializable
data class HentPersonResponse(
    val data: HentPersonRepsonse? = null,
    val errors: List<PdlError>? = null
)
