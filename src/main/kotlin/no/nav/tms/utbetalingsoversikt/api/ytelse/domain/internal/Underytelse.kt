package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal

import java.math.BigDecimal

data class Underytelse (
    val id:  Int,
    val beskrivelse:  String?,
    val satstype:  String?,
    val sats:  BigDecimal?,
    val antall:  Int?,
    val belop:  BigDecimal?,
)
