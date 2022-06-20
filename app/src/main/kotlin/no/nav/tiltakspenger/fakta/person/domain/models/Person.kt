package no.nav.tiltakspenger.fakta.person.domain.models

import no.nav.tiltakspenger.fakta.person.pdl.models.AdressebeskyttelseGradering
import java.time.LocalDate

data class Person(
    val foedselsdato: LocalDate?,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val adressebeskyttelseGradering: AdressebeskyttelseGradering?,
    val gtKommune: String?,
    val gtBydel: String?,
    val gtLand: String?,
    val barn: List<Barn>,
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "fødselsdato" to this.foedselsdato,
            "fornavn" to this.fornavn,
            "etternavn" to this.etternavn,
            "mellomnavn" to this.mellomnavn,
            "adressebeskyttelseGradering" to this.adressebeskyttelseGradering,
            "barn" to barn.map { it.toMap() },
            "gtKommune" to this.gtKommune,
            "gtBydel" to this.gtBydel,
            "gtLand" to this.gtLand,
        )
    }
}
