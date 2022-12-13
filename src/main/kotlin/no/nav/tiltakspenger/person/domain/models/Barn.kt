package no.nav.tiltakspenger.person.domain.models

import no.nav.tiltakspenger.person.pdl.models.AdressebeskyttelseGradering
import java.time.LocalDate

data class BarnIFolkeregisteret(
    val ident: String,
    override val fornavn: String,
    override val mellomnavn: String?,
    override val etternavn: String,
    override val fødselsdato: LocalDate,
    override val adressebeskyttelseGradering: AdressebeskyttelseGradering,
) : Personopplysninger

data class BarnUtenFolkeregisteridentifikator(
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val fødselsdato: LocalDate?,
    val statsborgerskap: String?,
)
