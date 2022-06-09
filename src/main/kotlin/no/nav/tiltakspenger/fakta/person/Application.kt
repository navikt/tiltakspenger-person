package no.nav.tiltakspenger.fakta.person

import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    RapidApplication.create(Configuration.asMap())
        .also {
            PersonService(it)
        }.start()
}
