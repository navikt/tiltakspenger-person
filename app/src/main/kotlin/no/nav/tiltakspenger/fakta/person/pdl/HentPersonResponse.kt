package no.nav.tiltakspenger.fakta.person.pdl

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.domain.models.Person
import no.nav.tiltakspenger.fakta.person.pdl.models.GeografiskTilknytning
import no.nav.tiltakspenger.fakta.person.pdl.models.PdlPerson
import no.nav.tiltakspenger.fakta.person.pdl.models.avklarFødsel
import no.nav.tiltakspenger.fakta.person.pdl.models.avklarGradering
import no.nav.tiltakspenger.fakta.person.pdl.models.avklarNavn
import no.nav.tiltakspenger.fakta.person.pdl.models.toBarn

@Serializable
data class HentPersonRepsonse(
    val hentPerson: PdlPerson?,
    val hentGeografiskTilknytning: GeografiskTilknytning?
)

const val FANT_IKKE_PERSON = "Fant ikke person"

@Serializable
data class HentPersonResponse(
    val data: HentPersonRepsonse? = null,
    val errors: List<PdlError> = emptyList(),
) {
    fun extractPerson(): Either<PDLClientError, PdlPerson> {
        if (this.errors.isNotEmpty()) {
            if (errors.any { it.message == FANT_IKKE_PERSON }) return PDLClientError.FantIkkePerson.left()
            return PDLClientError.UkjentFeil(this.errors).left()
        }
        return this.data?.hentPerson?.right()
            ?: PDLClientError.ResponsManglerPerson.left()
    }
    fun geografiskTilknytning(): GeografiskTilknytning? {
        return data?.hentGeografiskTilknytning
    }

    suspend fun toPerson(): Either<PDLClientError, Person> {
        return either {
            val person = extractPerson().bind()
            val navn = avklarNavn(person.navn).bind()
            val fødsel = avklarFødsel(person.foedsel)
            val adressebeskyttelse = avklarGradering(person.adressebeskyttelse)
            val geografiskTilknytning = geografiskTilknytning()
            Person(
                fornavn = navn.fornavn,
                mellomnavn = navn.mellomnavn,
                etternavn = navn.etternavn,
                fødselsdato = fødsel?.foedselsdato,
                adressebeskyttelseGradering = adressebeskyttelse?.gradering,
                barn = person.forelderBarnRelasjon.toBarn(),
                gtBydel = geografiskTilknytning?.gtBydel,
                gtKommune = geografiskTilknytning?.gtKommune,
                gtLand = geografiskTilknytning?.gtLand,
            )
        }
    }
}
