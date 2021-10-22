package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class Trekk (
    val id: Int,
    @SerialName("trekk_type") val trekkType: String,
    @SerialName("trekk_belop") @Serializable(with = BigDecimalSerializer::class) val trekkBelop: BigDecimal
)
