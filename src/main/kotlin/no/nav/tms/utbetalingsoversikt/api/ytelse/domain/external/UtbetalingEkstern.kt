package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class UtbetalingEkstern(
    val utbetaltTil: AktoerEkstern,
    val utbetalingsmetode: String,
    val utbetalingsstatus: String,
    val posteringsdato: String,
    val forfallsdato: String? = null,
    val utbetalingsdato: String? = null,
    val utbetalingNettobeloep: Double? = null,
    val utbetalingsmelding: String? = null,
    val utbetaltTilKonto: BankkontoEkstern? = null,
    val ytelseListe: List<YtelseEkstern> = emptyList(),
) {
    fun harKontonummer() = utbetaltTilKonto != null && utbetaltTilKonto.kontonummer.isNotBlank()
    val erUtbetalt = utbetalingsdato != null

    fun isInPeriod(fomDate: LocalDate, toDate: LocalDate) = ytelsesdato() in fomDate..toDate

    fun ytelsesdato() = LocalDate.parse(utbetalingsdato ?: forfallsdato)
}















