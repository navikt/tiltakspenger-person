package no.nav.tiltakspenger.person

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.mockk.mockk
import no.nav.tiltakspenger.person.pdl.PDLService

fun ApplicationTestBuilder.configureTestApplication(
    pdlService: PDLService = mockk(),
) {
    environment {
        config = ApplicationConfig("application.test.conf")
    }

    application {
        installAuthentication()
        applicationModule()
        installJacksonFeature()
    }
}
