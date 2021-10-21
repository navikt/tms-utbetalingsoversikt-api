package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import java.math.BigDecimal

data class YtelseskomponentEkstern(
    val ytelseskomponenttype: String?,
    val satsbeloep: BigDecimal?,
    val satstype: String?,
    val satsantall: Int?,
    val ytelseskomponentbeloep: BigDecimal?,
)
