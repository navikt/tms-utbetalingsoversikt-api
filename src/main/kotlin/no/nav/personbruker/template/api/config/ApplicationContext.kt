package no.nav.personbruker.template.api.config

import no.nav.personbruker.template.api.health.HealthService

class ApplicationContext {

    val httpClient = HttpClientBuilder.build()
    val healthService = HealthService(this)

}
