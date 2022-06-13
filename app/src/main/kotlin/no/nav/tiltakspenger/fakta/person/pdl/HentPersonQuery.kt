package no.nav.tiltakspenger.fakta.person.pdl

import kotlinx.serialization.Serializable

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
data class Person(
    val fornavn: String,
    val etternavn: String
)

@Serializable
data class HentPersonResponse(
    val data: Person,
    val errors: List<PdlError>? = null
)
