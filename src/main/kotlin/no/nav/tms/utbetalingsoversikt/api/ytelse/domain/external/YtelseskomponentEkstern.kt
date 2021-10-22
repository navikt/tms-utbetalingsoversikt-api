package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable

@Serializable
data class YtelseskomponentEkstern(
    val ytelseskomponenttype: String? = null,
    val satsbeloep: Double? = null,
    val satstype: String? = null,
    val satsantall: Int? = null,
    val ytelseskomponentbeloep: Double? = null,
)
