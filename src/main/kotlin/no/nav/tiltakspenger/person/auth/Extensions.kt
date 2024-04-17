package no.nav.tiltakspenger.person.auth

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authentication
import io.ktor.server.auth.principal
import no.nav.security.token.support.v2.TokenValidationContextPrincipal

internal fun ApplicationCall.getClaim(issuer: String, name: String): String? =
    this.authentication.principal<TokenValidationContextPrincipal>()
        ?.context
        ?.getClaims(issuer)
        ?.getStringClaim(name)

fun ApplicationCall.getFnrForTokenx(): String? {
    return this.getClaim("tokendings", "pid")
}
fun ApplicationCall.getFnrForAzureToken(): String? {
    return this.getClaim("azure", "ident")
}

fun ApplicationCall.token(): String {
    return this.principal<TokenValidationContextPrincipal>().asTokenString()
}

internal fun TokenValidationContextPrincipal?.asTokenString(): String =
    this?.context?.firstValidToken?.encodedToken
        ?: throw RuntimeException("no token found in call context")
