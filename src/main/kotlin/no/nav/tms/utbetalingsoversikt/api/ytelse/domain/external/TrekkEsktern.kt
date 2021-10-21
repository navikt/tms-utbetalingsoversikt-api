package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import java.math.BigDecimal

data class TrekkEsktern(
    val trekktype: String?,
    val trekkbeloep: BigDecimal?,
    val kreditor: String?,
)
