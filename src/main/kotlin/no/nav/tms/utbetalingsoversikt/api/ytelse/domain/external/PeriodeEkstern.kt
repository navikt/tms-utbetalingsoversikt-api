package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable

@Serializable
data class PeriodeEkstern(
    val fom: String,
    val tom: String
)
