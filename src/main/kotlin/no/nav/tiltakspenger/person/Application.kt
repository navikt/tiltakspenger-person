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
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.routing.routing
import mu.KotlinLogging
import no.nav.security.token.support.v2.RequiredClaims
import no.nav.security.token.support.v2.tokenValidationSupport
import no.nav.tiltakspenger.person.auth.Configuration
import no.nav.tiltakspenger.person.auth.Configuration.httpPort
import no.nav.tiltakspenger.person.auth.TokenProvider
import no.nav.tiltakspenger.person.pdl.AzureRoutes
import no.nav.tiltakspenger.person.pdl.PDLClient
import no.nav.tiltakspenger.person.pdl.PDLService
import no.nav.tiltakspenger.person.pdl.TokenxRoutes

enum class ISSUER(val value: String) {
    TOKENDINGS("tokendings"),
    AZURE("azure"),
}

fun main() {
    System.setProperty("logback.configurationFile", Configuration.logbackConfigurationFile())
    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")

    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
    }

    log.info { "Starting tiltakspenger-person" }

    embeddedServer(Netty, port = httpPort(), module = Application::applicationModule).start(wait = true)
    /*val pdlService = PDLService(pdlClient = PDLClient(TokenProvider()))

    RapidApplication.create(AuthConfiguration.rapidsAndRivers)
        .apply {
            PersonopplysningerService(
                arapidsConnection = this,
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
        }.start()*/
}

fun Application.applicationModule() {
    val pdlClient = PDLClient(TokenProvider())
    val pdlService = PDLService(pdlClient)

    installJacksonFeature()
    installAuthentication()
    routing {
        authenticate(ISSUER.TOKENDINGS.value) {
            TokenxRoutes(pdlService)
        }
        authenticate(ISSUER.AZURE.value) {
            AzureRoutes(pdlService)
        }
        healthRoutes()
    }
    install(CallLogging) {
        callIdMdc("call-id")
        disableDefaultColors()
        filter { call ->
            !call.request.path().startsWith("/isalive") &&
                !call.request.path().startsWith("/isready")
        }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            "Status: $status, HTTP method: $httpMethod"
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
