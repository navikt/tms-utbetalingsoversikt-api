package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal

import java.math.BigDecimal

data class Trekk (
    val id: Int,
    val trekkType: String,
    val trekkBelop: BigDecimal
)
