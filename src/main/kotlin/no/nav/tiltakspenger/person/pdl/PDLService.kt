package no.nav.tiltakspenger.person.pdl

import arrow.core.Either
import arrow.core.left
import no.nav.tiltakspenger.person.Person

class PDLService(private val pdlClient: PDLClient) {

    suspend fun hentPerson(ident: String): Either<PDLClientError, Person> {
        return pdlClient.hentPerson(ident).map { (person, barnsIdenter) ->
            person.copy(
                barn = barnsIdenter.map { ident ->
                    pdlClient.hentPersonSomBarn(ident)
                        .fold({ return it.left() }, { it })
                }
            )
        }
    }
}
