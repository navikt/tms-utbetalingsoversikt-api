package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.utbetaling.UtbetalingSerializationException
import java.time.LocalDate

private val log = KotlinLogging.logger { }


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
    fun maskertKontonummer(): String {
        val kontonummer = utbetaltTilKonto?.kontonummer
        return when {
            !harKontonummer() -> utbetalingsmetode
            kontonummer != null && kontonummer.length <= 5 -> kontonummer
            else -> maskAllButFinalCharacters(kontonummer?.replace(" ", ""), 5)
        }
    }

    private fun maskAllButFinalCharacters(kontonummer: String?, numberUnmasked: Int): String =
        kontonummer
            ?.let {
                val numberMaskedChars = it.length - numberUnmasked
                return "${"x".repeat(5)}${it.substring(numberMaskedChars)}"
            } ?: ""

    fun erUtbetalt(now: LocalDate): Boolean {
        val harUtbetalingsdato = utbetalingsdato != null
        val ytelsesdato = this.ytelsesdato() ?: throw IllegalStateException("Feil i filtrering - mangler ytelsesdato.")
        if (ytelsesdato.isBefore(now) && !harUtbetalingsdato) {
            log.info { "Utbetaling med utgått forfallsdato men uten utbetalingsdato, [forfallsdato: ${this.forfallsdato} utbetalingsdato: ${this.utbetalingsdato}]" }
        }
        return ytelsesdato.isBefore(now) || ytelsesdato.isEqual(now) && harUtbetalingsdato
    }

    fun isInPeriod(fomDate: LocalDate, tomDate: LocalDate): Boolean {
        return when {
            utbetalingsdato != null -> LocalDate.parse(utbetalingsdato) in fomDate..tomDate
            forfallsdato != null -> true
            else -> false
        }
    }

    fun ytelsesdato(): LocalDate? = utbetalingsdato?.let { LocalDate.parse(it) }
        ?: forfallsdato?.let { LocalDate.parse(it) }

    companion object {
        fun List<UtbetalingEkstern>.sisteUtbetaling(now: LocalDate): UtbetalingEkstern? =
            this.filter { it.erUtbetalt(now) }
                .maxByOrNull { it.utbetalingsdato.toLocalDate() }

        fun List<UtbetalingEkstern>.nesteUtbetaling(now: LocalDate): UtbetalingEkstern? =
            this.filter { !it.erUtbetalt(now) }
                .minByOrNull { it.forfallsdato.toLocalDate() }


        fun String?.toLocalDate(): LocalDate = try {
            LocalDate.parse(this)
        } catch (e: Exception) {
            throw UtbetalingSerializationException("Fant ikke utbetalingsdato, ${e.message}")
        }

    }

}















