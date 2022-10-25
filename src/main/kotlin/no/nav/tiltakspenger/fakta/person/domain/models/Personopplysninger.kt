package no.nav.tiltakspenger.fakta.person.domain.models

import no.nav.tiltakspenger.fakta.person.pdl.models.AdressebeskyttelseGradering
import java.time.LocalDate

interface Personopplysninger {
    val fødselsdato: LocalDate
    val fornavn: String
    val mellomnavn: String?
    val etternavn: String
    val adressebeskyttelseGradering: AdressebeskyttelseGradering
}
