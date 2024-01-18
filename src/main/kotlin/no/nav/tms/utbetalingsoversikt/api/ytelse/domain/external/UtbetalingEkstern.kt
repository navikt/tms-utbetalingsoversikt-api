package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.v2.UtbetalingSerializationException
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

    val erUtbetalt = utbetalingsdato != null

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
        fun List<UtbetalingEkstern>.sisteUtbetaling(): UtbetalingEkstern? =
            this.filter { it.utbetalingsdato != null }
                .maxByOrNull { it.utbetalingsdato.toLocalDate() }

        fun List<UtbetalingEkstern>.nesteUtbetaling(): UtbetalingEkstern? =
            this.filter { (it.ytelsesdato()?.isAfter(LocalDate.now().minusDays(1)) ?: false) }
                .minByOrNull { it.forfallsdato.toLocalDate() }

        fun String?.toLocalDate(): LocalDate = try {
            LocalDate.parse(this)
        } catch (e: Exception) {
            throw UtbetalingSerializationException("Fant ikke utbetalingsdato, ${e.message}")
        }

    }

}















