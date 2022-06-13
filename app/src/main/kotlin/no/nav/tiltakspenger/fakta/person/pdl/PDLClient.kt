package no.nav.tiltakspenger.fakta.person.pdl

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.Configuration.getPDLUrl
import no.nav.tiltakspenger.azureAuth.azureClient
import no.nav.tiltakspenger.azureAuth.OauthConfig
import no.nav.tiltakspenger.fakta.person.Configuration

val url = getPDLUrl()
val client = azureClient(
    OauthConfig.fromEnv(
        scope = Configuration.getPdlScope(),
    )
)

object PDLClient {
    suspend fun hentPerson(ident: String): HentPersonResponse {
        val res: HentPersonResponse = client.post(url) {
            setBody(hentPersonQuery(ident))
            contentType(ContentType.Application.Json)
        }.body()
        return res
    }
}

@Serializable
data class GraphqlQuery(
    val query: String,
    val variables: Map<String, String>
)

