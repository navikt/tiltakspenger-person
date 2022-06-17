package no.nav.tiltakspenger.fakta.person.pdl.models

import java.time.LocalDate
import java.time.LocalDateTime

data class Barn(
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val foedselsdato: LocalDate?,
) {
    fun toMap(): Map<String, String?> {
        return mapOf(
            "fornavn" to fornavn,
            "mellomnavn" to mellomnavn,
            "etternavn" to etternavn,
            "foedselsdato" to foedselsdato.toString(),
        )
    }
}

fun List<ForelderBarnRelasjon>.toBarn(): List<Barn> {
    return this
        .filter { it.relatertPersonsRolle == ForelderBarnRelasjonRolle.BARN }
        .groupBy { it.relatertPersonsIdent }
        .mapNotNull { (_, barn) ->
            barn.maxByOrNull { getEndringstidspunktOrNull(it) ?: LocalDateTime.MIN }
        }
        .map {
            val barn = it.relatertPersonUtenFolkeregisteridentifikator
            val navn = barn?.navn
            Barn(
                fornavn = navn?.fornavn,
                mellomnavn = navn?.fornavn,
                etternavn = navn?.fornavn,
                foedselsdato = barn?.foedselsdato
            )
        }
}

data class Person(
    val fødsel: Fødsel?,
    val navn: Navn,
    val adressebeskyttelse: Adressebeskyttelse?,
    val geografiskTilknytning: GeografiskTilknytning?,
    val relasjoner: List<ForelderBarnRelasjon>,
    private val barn: List<Barn> = relasjoner.toBarn()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "fødselsdato" to this.fødsel?.foedselsdato,
            "fornavn" to this.navn.fornavn,
            "etternavn" to this.navn.etternavn,
            "mellomnavn" to this.navn.mellomnavn,
            "adressebeskyttelseGradering" to this.adressebeskyttelse?.gradering,
            "barn" to barn.map { it.toMap() }
        )
    }
}
