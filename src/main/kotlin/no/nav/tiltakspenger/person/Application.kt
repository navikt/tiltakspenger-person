package no.nav.tiltakspenger.person

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.security.token.support.v2.RequiredClaims
import no.nav.security.token.support.v2.tokenValidationSupport
import no.nav.tiltakspenger.person.auth.AzureTokenProvider
import no.nav.tiltakspenger.person.pdl.PDLAzureRoutes
import no.nav.tiltakspenger.person.pdl.PDLClient
import no.nav.tiltakspenger.person.pdl.PDLService
import no.nav.tiltakspenger.person.pdl.PDLTokenxRoutes

enum class ISSUER(val value: String) {
    TOKENDINGS("tokendings"),
    AZURE("azure"),
}

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

    // embeddedServer(Netty, port = httpPort(), module = Application::applicationModule).start(wait = true)

    val pdlService = PDLService(pdlClient = PDLClient(getToken = tokenProvider::getToken))
    RapidApplication.create(Configuration.rapidsAndRivers)
        .apply {
            PersonopplysningerService(
                rapidsConnection = this,
                pdlService = pdlService,
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

fun Application.applicationModule() {
    val tokenProvider = AzureTokenProvider()
    val pdlClient = PDLClient(getToken = tokenProvider::getToken)
    val pdlService = PDLService(pdlClient)

    installJacksonFeature()
    installAuthentication()
    routing {
        authenticate(ISSUER.TOKENDINGS.value) {
            PDLTokenxRoutes(pdlService)
        }
        authenticate(ISSUER.AZURE.value) {
            PDLAzureRoutes(pdlService)
        }
    }
}

fun Application.installAuthentication() {
    val config = ApplicationConfig("application.conf")
    install(Authentication) {
        tokenValidationSupport(
            name = ISSUER.TOKENDINGS.value,
            config = config,
            requiredClaims = RequiredClaims(
                issuer = ISSUER.TOKENDINGS.value,
                claimMap = arrayOf("acr=Level4", "acr=idporten-loa-high"),
                combineWithOr = false,
            ),
        )
        tokenValidationSupport(
            name = ISSUER.AZURE.value,
            config = config,
            requiredClaims = RequiredClaims(
                issuer = ISSUER.AZURE.value,
                claimMap = arrayOf(),
                combineWithOr = false,
            ),
        )
    }
}

fun Application.installJacksonFeature() {
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            registerModule(JavaTimeModule())
            registerModule(KotlinModule.Builder().build())
        }
    }
}
