package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal

import java.time.LocalDate

data class Periode (
    val fom: LocalDate,
    val tom: LocalDate,
)
