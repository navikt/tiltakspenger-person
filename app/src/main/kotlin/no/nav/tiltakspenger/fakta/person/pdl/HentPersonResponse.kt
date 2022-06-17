package no.nav.tiltakspenger.fakta.person.pdl

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.pdl.models.GeografiskTilknytning
import no.nav.tiltakspenger.fakta.person.pdl.models.PdlPerson

@Serializable
data class HentPersonRepsonse(
    val hentPerson: PdlPerson?,
    val hentGeografiskTilknytning: GeografiskTilknytning?
)

@Serializable
data class HentPersonResponse(
    val data: HentPersonRepsonse? = null,
    val errors: List<PdlError>? = null,
) {
    fun extractPerson(): Either<PDLClientError, PdlPerson> {
        if (this.errors != null && this.errors.isNotEmpty()) {
            return PDLClientError.UkjentFeil(this.errors).left()
        }
        return this.data?.hentPerson?.right()
            ?: PDLClientError.ResponsManglerPerson.left()
    }
    fun geografiskTilknytning(): GeografiskTilknytning? {
        return data?.hentGeografiskTilknytning
    }
}
