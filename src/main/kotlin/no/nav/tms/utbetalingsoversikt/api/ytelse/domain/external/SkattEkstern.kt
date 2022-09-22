package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable

@Serializable
data class SkattEkstern(
    val skattebeloep: Double?
)
