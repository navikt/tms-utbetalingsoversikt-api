package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer

import no.nav.tms.utbetalingsoversikt.api.utbetaling.YtelseIdUtil
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Periode
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Rettighetshaver
import java.time.LocalDate

object HovedytelseTransformer {

    fun toHovedYtelse(utbetaling: UtbetalingEkstern): List<Hovedytelse> {
        return utbetaling.ytelseListe.map { ytelseEkstern ->
            Hovedytelse(
                id = YtelseIdUtil.calculateId(utbetaling.posteringsdato, ytelseEkstern),
                rettighetshaver = Rettighetshaver(
                    ytelseEkstern.rettighetshaver.ident,
                    ytelseEkstern.rettighetshaver.navn
                ),
                ytelse = ytelseEkstern.ytelsestype ?: "",
                status = utbetaling.utbetalingsstatus,
                ytelseDato = ytelseDato(utbetaling),
                forfallDato = ytelseDato(utbetaling),
                utbetaltTil = utbetaling.utbetaltTil.navn,
                ytelsePeriode = Periode(
                    fom = LocalDate.parse(ytelseEkstern.ytelsesperiode.fom),
                    tom = LocalDate.parse(ytelseEkstern.ytelsesperiode.tom)
                ),
                kontonummer = kontonummerVerdi(utbetaling),
                underytelser = ytelseEkstern.ytelseskomponentListe?.let { komponentList ->
                    UnderytelseTransformer.createUnderytelser(komponentList)
                } ?: emptyList(),
                trekk = TrekkTransformer.createTrekkList(ytelseEkstern),
                erUtbetalt = utbetaling.erUtbetalt,
                melding = utbetaling.utbetalingsmelding ?: ""
            )
        }
    }

    private fun ytelseDato(utbetaling: UtbetalingEkstern): LocalDate? =
        utbetaling.utbetalingsdato?.parseLocalDate() ?: utbetaling.forfallsdato?.parseLocalDate()

    private fun kontonummerVerdi(utbetaling: UtbetalingEkstern): String {
        val kontonummer = utbetaling.utbetaltTilKonto?.kontonummer
        return when {
            !utbetaling.harKontonummer() -> utbetaling.utbetalingsmetode
            kontonummer != null && kontonummer.length <= 5 -> kontonummer
            else -> maskAllButFinalCharacters(kontonummer.removeWhitespace(), 5)
        }
    }

    private fun maskAllButFinalCharacters(kontonummer: String?, numberUnmasked: Int): String =
        kontonummer
            ?.let {
                val numberMaskedChars = it.length - numberUnmasked
                return "${"x".repeat(numberMaskedChars)}${it.substring(numberMaskedChars)}"
            } ?: ""
}

private fun String.parseLocalDate(): LocalDate = LocalDate.parse(this)
private fun String?.removeWhitespace() = this?.replace(" ", "")