package no.nav.tiltakspenger.person

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.person.auth.AzureTokenProvider
import no.nav.tiltakspenger.person.pdl.PDLClient
import no.nav.tiltakspenger.person.pdl.PDLService

fun main() {
    System.setProperty("logback.configurationFile", "egenLogback.xml")
    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")

    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
    }
    val tokenProvider = AzureTokenProvider()
    log.info { "Starting tiltakspenger-person" }
    RapidApplication.create(Configuration.rapidsAndRivers)
        .apply {
            PersonopplysningerService(
                rapidsConnection = this,
                pdlService = PDLService(pdlClient = PDLClient(getToken = tokenProvider::getToken)),
            )

            register(object : RapidsConnection.StatusListener {
                override fun onStartup(rapidsConnection: RapidsConnection) {
                    log.info { "Starting tiltakspenger-person" }
                }

                override fun onShutdown(rapidsConnection: RapidsConnection) {
                    log.info { "Stopping tiltakspenger-person" }
                    super.onShutdown(rapidsConnection)
                }
            })
        }.start()
}
