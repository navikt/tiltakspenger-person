package no.nav.tiltakspenger.person.pdl.models

import no.nav.tiltakspenger.person.Adressebeskyttelse
import no.nav.tiltakspenger.person.Fødsel

data class PdlPerson(
    val navn: List<Navn>,
    val foedsel: List<Fødsel>,
    val adressebeskyttelse: List<Adressebeskyttelse>,
    val forelderBarnRelasjon: List<ForelderBarnRelasjon>,
)
