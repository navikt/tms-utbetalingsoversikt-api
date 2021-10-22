package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Rettighetshaver (
    @SerialName("aktoer_id") val aktoerId: String,
    val navn: String?
)
