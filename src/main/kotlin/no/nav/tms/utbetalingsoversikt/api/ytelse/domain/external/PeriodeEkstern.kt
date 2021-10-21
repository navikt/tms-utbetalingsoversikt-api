package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import java.time.LocalDate

data class PeriodeEkstern(
    val fom: LocalDate,
    val tom: LocalDate,
)
