package no.nav.personbruker.template.api.health

interface HealthCheck {

    suspend fun status(): HealthStatus

}
