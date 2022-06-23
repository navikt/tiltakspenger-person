package no.nav.tiltakspenger.fakta.person

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication

private val LOG = KotlinLogging.logger {}
fun main() {
    Thread.setDefaultUncaughtExceptionHandler { _, e -> LOG.error(e) { e.message } }
    LOG.info { "Starting tiltakspenger-fakta-person" }
    RapidApplication.create(Configuration.asMap())
        .also {
            PersonService(it)
        }.start()
}
