package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Trekk (
    val id: Int,
    @SerialName("trekk_type") val trekkType: String,
    @SerialName("trekk_belop") val trekkBelop: Double
)
