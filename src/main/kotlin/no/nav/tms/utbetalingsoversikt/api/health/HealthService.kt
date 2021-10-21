package no.nav.tms.utbetalingsoversikt.api.health

import no.nav.tms.utbetalingsoversikt.api.config.ApplicationContext

class HealthService(private val applicationContext: ApplicationContext) {

    suspend fun getHealthChecks(): List<HealthStatus> {
        return emptyList()
    }
}
