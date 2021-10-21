package no.nav.tms.utbetalingsoversikt.api.health

interface HealthCheck {

    suspend fun status(): HealthStatus

}
