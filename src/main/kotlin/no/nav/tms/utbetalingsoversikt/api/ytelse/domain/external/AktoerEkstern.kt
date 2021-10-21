package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable

@Serializable
data class AktoerEkstern(
    val aktoertype: AktoertypeEsktern,
    val aktoerId: String,
    val navn: String?,
)
