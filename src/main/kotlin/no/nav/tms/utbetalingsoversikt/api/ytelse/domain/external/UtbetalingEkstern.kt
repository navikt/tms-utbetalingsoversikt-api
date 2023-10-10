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

    fun isInPeriod(fomDate: LocalDate, tomDate: LocalDate): Boolean {
        return when {
            utbetalingsdato != null -> LocalDate.parse(utbetalingsdato) in fomDate..tomDate
            forfallsdato != null -> true
            else -> false
        }
    }

    fun ytelsesdato() = utbetalingsdato?.let { LocalDate.parse(it) }
        ?: forfallsdato?.let { LocalDate.parse(it) }
}















