package no.nav.tiltakspenger.fakta.person.domain.models

import no.nav.tiltakspenger.fakta.person.pdl.models.AdressebeskyttelseGradering
import java.time.LocalDate

data class Person(
    override val fødselsdato: LocalDate,
    override val fornavn: String,
    override val mellomnavn: String?,
    override val etternavn: String,
    override val adressebeskyttelseGradering: AdressebeskyttelseGradering,
    val gtKommune: String?,
    val gtBydel: String?,
    val gtLand: String?,
    val barn: List<BarnIFolkeregisteret>,
    val barnUtenFolkeregisteridentifikator: List<BarnUtenFolkeregisteridentifikator>,
) : Personopplysninger
