package no.nav.tiltakspenger.person.pdl

import arrow.core.Either
import arrow.core.left
import no.nav.tiltakspenger.libs.person.Person

class PDLService(private val pdlClient: PDLClient) {

    suspend fun hentPersonMedTokenx(ident: String, subjectToken: String): Either<PDLClientError, Person> {
        return pdlClient.hentPersonMedTokenx(ident, subjectToken).map { (person, barnsIdenter) ->
            person.copy(
                barn = barnsIdenter.map { ident ->
                    pdlClient.hentPersonSomBarn(ident)
                        .fold({ return it.left() }, { it })
                },
            )
        }
    }

    suspend fun hentPersonMedAzure(ident: String): Either<PDLClientError, Person> {
        return pdlClient.hentPersonMedAzure(ident).map { (person, barnsIdenter) ->
            person.copy(
                barn = barnsIdenter.map { ident ->
                    pdlClient.hentPersonSomBarn(ident)
                        .fold({ return it.left() }, { it })
                },
            )
        }
    }
}
