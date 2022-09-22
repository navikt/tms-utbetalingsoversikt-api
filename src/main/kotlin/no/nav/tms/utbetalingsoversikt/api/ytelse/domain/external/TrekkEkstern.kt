package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable

@Serializable
data class TrekkEkstern(
    val trekktype: String? = null,
    val trekkbeloep: Double? = null,
    val kreditor: String? = null
)
