package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable

@Serializable
data class TrekkEsktern(
    val trekktype: String? = null,
    val trekkbeloep: Double? = null,
    val kreditor: String? = null
)
