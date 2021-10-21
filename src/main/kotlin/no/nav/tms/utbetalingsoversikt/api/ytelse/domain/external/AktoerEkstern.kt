package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

data class AktoerEkstern(
    val aktoertype: AktoertypeEsktern,
    val aktoerId: String,
    val navn: String?,
)
