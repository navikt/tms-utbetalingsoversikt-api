package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class Trekk (
    val id: Int,
    val trekkType: String,
    @Serializable(with = BigDecimalSerializer::class) val trekkBelop: BigDecimal
)
