package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.EksternModelObjectMother
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.*
import org.amshove.kluent.*
import org.junit.jupiter.api.Test
import java.lang.Integer.max

internal class HovedytelseTransformerTest {

    @Test
    fun `should transform EksternYtelse`() {
        val original = EksternModelObjectMother.giveMeUtbetalingEkstern()

        val transformed = HovedytelseTransformer.toHovedYtelse(original)

        validateHovedYtelser(original, transformed)
    }

    private fun validateHovedYtelser(expected: UtbetalingEkstern, toValidate: List<Hovedytelse>) {
        toValidate.forEach { hovedytelse ->
            validateRettighetshaver(expected, hovedytelse.rettighetshaver)
            validatePeriode(expected, hovedytelse.ytelsePeriode)
            validateKontonummer(expected, hovedytelse.kontonummer)
            validateUnderytelser(expected, hovedytelse.underytelser)
            validateTrekk(expected, hovedytelse.trekk)
            hovedytelse.ytelse `should be in` expected.ytelseListe.map { it.ytelsestype }
            hovedytelse.status `should be equal to` expected.utbetalingsstatus
            hovedytelse.ytelseDato.toString() `should be equal to` expected.utbetalingsdato
            hovedytelse.forfallDato.toString() `should be equal to` expected.utbetalingsdato
            hovedytelse.utbetaltTil `should be equal to` expected.utbetaltTil.navn
            hovedytelse.melding `should be equal to` expected.utbetalingsmelding
            hovedytelse.erUtbetalt `should be equal to` true
        }
    }

    private fun validateRettighetshaver(expected: UtbetalingEkstern, toValidate: Rettighetshaver) {
        val original = expected.ytelseListe.map { it.rettighetshaver }.firstOrNull()

        toValidate.navn `should be equal to` original?.navn
        toValidate.aktoerId `should be equal to` original?.aktoerId
    }

    private fun validatePeriode(expected: UtbetalingEkstern, toValidate: Periode) {
        val expectedPairs = expected.ytelseListe.map { it.ytelsesperiode }.map { it.fom to it.tom }

        val pairToValidate = toValidate.fom.toString() to toValidate.tom.toString()

        pairToValidate `should be in` expectedPairs
    }

    private fun validateKontonummer(expected: UtbetalingEkstern, toValidate: String) {
        if (expected.utbetaltTilKonto != null && expected.utbetaltTilKonto!!.kontonummer.isNotBlank()) {
            expected.utbetaltTilKonto!!.kontonummer `should contain`  toValidate.substring(max(toValidate.length -5, 0))
        } else {
            toValidate `should be equal to` expected.utbetalingsmetode
        }
    }

    fun validateUnderytelser(expected: UtbetalingEkstern, underytelser: List<Underytelse>) {
        underytelser.size `should be equal to` expected.ytelseListe.size

        val antallOriginal = expected.ytelseListe
            .flatMap { it.ytelseskomponentListe ?: emptyList() }
            .sumBy { it.satsantall ?: 0 }
        underytelser.sumBy { it.antall ?: 0 } `should be less or equal to` antallOriginal

        val belopOriginal = expected.ytelseListe
            .flatMap { it.ytelseskomponentListe ?: emptyList() }
            .sumByDouble { it.satsbeloep ?: 0.0 }
        underytelser.sumByDouble { it.belop ?: 0.0 } `should be less or equal to` belopOriginal
    }

    fun validateTrekk(expected: UtbetalingEkstern, trekk: List<Trekk>) {
        val skattOriginal = expected.ytelseListe
            .flatMap { it.skattListe ?: emptyList() }
            .sumByDouble { it.skattebeloep ?: 0.0 }

        val trekkOriginal = expected.ytelseListe
            .flatMap { it.trekkListe ?: emptyList() }
            .sumByDouble { it.trekkbeloep ?: 0.0 }

        trekk.sumByDouble { it.trekkBelop } `should be greater or equal to` skattOriginal + trekkOriginal
    }
}
