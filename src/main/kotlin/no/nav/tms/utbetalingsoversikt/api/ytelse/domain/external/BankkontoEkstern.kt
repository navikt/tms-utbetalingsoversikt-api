package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable

@Serializable
data class BankkontoEkstern(
    val kontonummer: String,
    val kontotype: String,
)
