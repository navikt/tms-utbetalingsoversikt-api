package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal

import java.time.LocalDate

data class Hovedytelse(
    val id: Int,
    val ytelse: String,
    val status: String,
    val ytelseDato: LocalDate?,
    val forfallDato: LocalDate?,
    val ytelsePeriode: Periode?,
    val utbetaltTil: String?,
    val kontonummer: String,
    val underytelser: List<Underytelse>,
    val trekk: List<Trekk>,
    val erUtbetalt: Boolean,
    val rettighetshaver: Rettighetshaver,
    val melding: String
)
