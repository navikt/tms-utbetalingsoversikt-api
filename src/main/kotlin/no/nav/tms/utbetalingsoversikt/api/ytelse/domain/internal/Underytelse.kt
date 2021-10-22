package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class Underytelse (
    val id:  Int,
    val beskrivelse:  String?,
    val satstype:  String?,
    @Serializable(with = BigDecimalSerializer::class) val sats:  BigDecimal?,
    val antall:  Int?,
    @Serializable(with = BigDecimalSerializer::class) val belop:  BigDecimal?,
)
