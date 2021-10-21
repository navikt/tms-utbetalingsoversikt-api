package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.AktoerEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.PeriodeEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Periode
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Rettighetshaver
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Underytelse
import org.slf4j.LoggerFactory
import java.time.LocalDate

object HovedytelseTransformer {

    val log = LoggerFactory.getLogger(HovedytelseTransformer::class.java)

    fun toHovedYtelse(utbetaling: UtbetalingEkstern): List<Hovedytelse> {
        return utbetaling.ytelseListe.map { ytelseEkstern ->
            Hovedytelse(
                id = ytelseEkstern.hashCode(),
                rettighetshaver = createRettighetshaver(ytelseEkstern.rettighetshaver),
                ytelse = ytelseEkstern.ytelsestype?: "",
                status = utbetaling.utbetalingsstatus,
                ytelseDato = determineYtelseDato(utbetaling),
                forfallDato = determineYtelseDato(utbetaling),
                utbetaltTil = utbetaling.utbetaltTil.navn,
                ytelsePeriode = createPeriode(ytelseEkstern.ytelsesperiode),
                kontonummer = KontonummerTransformer.determineKontonummerVerdi(utbetaling),
                underytelser = createUnderYtelser(ytelseEkstern),
                trekk = TrekkTransformer.createTrekkList(ytelseEkstern),
                erUtbetalt = isUtbetalt(utbetaling),
                melding = utbetaling.utbetalingsmelding?: ""
            )
        }
    }

    private fun createRettighetshaver(aktoer: AktoerEkstern): Rettighetshaver {
        return Rettighetshaver(aktoer.aktoerId, aktoer.navn)
    }

    private fun isUtbetalt(utbetaling: UtbetalingEkstern): Boolean {
        return utbetaling.utbetalingsdato != null
    }

    private fun determineYtelseDato(utbetaling: UtbetalingEkstern): LocalDate? {
        return if (isUtbetalt(utbetaling)) {
            utbetaling.utbetalingsdato
        } else {
            utbetaling.forfallsdato
        }
    }

    private fun createPeriode(ytelsesperiode: PeriodeEkstern?): Periode? {
        return if (ytelsesperiode == null) {
            log.debug("Ytelsesperiode er tom, setter ikke fom og tom felt for periode")
            null
        } else {
            Periode(ytelsesperiode.fom, ytelsesperiode.tom)
        }
    }

    private fun createUnderYtelser(ytelse: YtelseEkstern): List<Underytelse> {
        return if (ytelse.ytelseskomponentListe != null) {
            UnderytelseTransformer.createUnderytelser(ytelse.ytelseskomponentListe)
        } else {
            emptyList()
        }
    }
}
