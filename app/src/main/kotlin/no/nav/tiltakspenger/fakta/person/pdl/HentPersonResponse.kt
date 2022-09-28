package no.nav.tiltakspenger.fakta.person.pdl

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.domain.models.BarnIFolkeregisteret
import no.nav.tiltakspenger.fakta.person.domain.models.Person
import no.nav.tiltakspenger.fakta.person.pdl.models.GeografiskTilknytning
import no.nav.tiltakspenger.fakta.person.pdl.models.PdlPerson
import no.nav.tiltakspenger.fakta.person.pdl.models.avklarFødsel
import no.nav.tiltakspenger.fakta.person.pdl.models.avklarGradering
import no.nav.tiltakspenger.fakta.person.pdl.models.avklarNavn
import no.nav.tiltakspenger.fakta.person.pdl.models.toBarnUtenforFolkeregisteret
import no.nav.tiltakspenger.fakta.person.pdl.models.toIdenterForBarnIFolkeregisteret

@Serializable
data class PdlResponseData(
    val hentPerson: PdlPerson?,
    val hentGeografiskTilknytning: GeografiskTilknytning?,
)

const val FANT_IKKE_PERSON = "Fant ikke person"

@Serializable
data class HentPersonResponse(
    val data: PdlResponseData? = null,
    val errors: List<PdlError> = emptyList(),
) {
    private fun extractPerson(): Either<PDLClientError, PdlPerson> {
        return if (this.errors.isNotEmpty()) {
            if (errors.any { it.message == FANT_IKKE_PERSON }) PDLClientError.FantIkkePerson.left()
            else PDLClientError.UkjentFeil(this.errors).left()
        } else this.data?.hentPerson?.right()
            ?: PDLClientError.ResponsManglerPerson.left()
    }

    private fun geografiskTilknytning(): GeografiskTilknytning? {
        return data?.hentGeografiskTilknytning
    }

    suspend fun toPerson(): Either<PDLClientError, Pair<Person, List<String>>> {
        return either {
            val person = extractPerson().bind()
            val navn = avklarNavn(person.navn).bind()
            val fødsel = avklarFødsel(person.foedsel).bind()
            val adressebeskyttelse = avklarGradering(person.adressebeskyttelse).bind()
            val geografiskTilknytning = geografiskTilknytning()

            Person(
                fornavn = navn.fornavn,
                mellomnavn = navn.mellomnavn,
                etternavn = navn.etternavn,
                fødselsdato = fødsel.foedselsdato,
                adressebeskyttelseGradering = adressebeskyttelse,
                barn = emptyList(),
                barnUtenFolkeregisteridentifikator = person.forelderBarnRelasjon.toBarnUtenforFolkeregisteret(),
                gtBydel = geografiskTilknytning?.gtBydel,
                gtKommune = geografiskTilknytning?.gtKommune,
                gtLand = geografiskTilknytning?.gtLand,
            ) to person.forelderBarnRelasjon.toIdenterForBarnIFolkeregisteret()
        }
    }

    suspend fun toBarn(ident: String): Either<PDLClientError, BarnIFolkeregisteret> {
        return either {
            val person = extractPerson().bind()
            val navn = avklarNavn(person.navn).bind()
            val fødsel = avklarFødsel(person.foedsel).bind()
            val adressebeskyttelse = avklarGradering(person.adressebeskyttelse).bind()

            BarnIFolkeregisteret(
                ident = ident,
                fornavn = navn.fornavn,
                mellomnavn = navn.mellomnavn,
                etternavn = navn.etternavn,
                fødselsdato = fødsel.foedselsdato,
                adressebeskyttelseGradering = adressebeskyttelse,
            )
        }
    }
}
