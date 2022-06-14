package no.nav.tiltakspenger.fakta.person.pdl

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.tiltakspenger.fakta.person.Configuration.getPDLUrl
import no.nav.tiltakspenger.azureAuth.azureClient
import no.nav.tiltakspenger.azureAuth.OauthConfig
import no.nav.tiltakspenger.fakta.person.Configuration

val url = getPDLUrl()
const val INDIVIDSTONAD = "IND"

class PDLClient(val client: HttpClient = azureClient(
    OauthConfig.fromEnv(
        scope = Configuration.getPdlScope(),
    )
)) {
    suspend fun hentPerson(ident: String): HentPersonResponse {
        val res: HentPersonResponse = client.post(url) {
            accept(ContentType.Application.Json)
            header("Tema", INDIVIDSTONAD)
            contentType(ContentType.Application.Json)
            setBody(hentPersonQuery(ident))
        }.body()
        return res
    }
}

