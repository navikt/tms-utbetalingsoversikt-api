package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer

import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.EksternModelObjectMother
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.*
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
            hovedytelse.ytelse shouldBeIn expected.ytelseListe.map { it.ytelsestype }
            hovedytelse.status shouldBe expected.utbetalingsstatus
            hovedytelse.ytelseDato.toString() shouldBe expected.utbetalingsdato
            hovedytelse.forfallDato.toString() shouldBe expected.utbetalingsdato
            hovedytelse.utbetaltTil shouldBe expected.utbetaltTil.navn
            hovedytelse.melding shouldBe expected.utbetalingsmelding
            hovedytelse.erUtbetalt shouldBe true
        }
    }

    private fun validateRettighetshaver(expected: UtbetalingEkstern, toValidate: Rettighetshaver) {
        val original = expected.ytelseListe.map { it.rettighetshaver }.firstOrNull()

        toValidate.navn shouldBe original?.navn
        toValidate.aktoerId shouldBe original?.ident
    }

    private fun validatePeriode(expected: UtbetalingEkstern, toValidate: Periode) {
        val expectedPairs = expected.ytelseListe.map { it.ytelsesperiode }.map { it.fom to it.tom }

        val pairToValidate = toValidate.fom.toString() to toValidate.tom.toString()

        pairToValidate shouldBeIn expectedPairs
    }

    private fun validateKontonummer(expected: UtbetalingEkstern, toValidate: String) {
        if (expected.utbetaltTilKonto != null && expected.utbetaltTilKonto!!.kontonummer.isNotBlank()) {
            expected.utbetaltTilKonto!!.kontonummer shouldContain toValidate.substring(max(toValidate.length -5, 0))
        } else {
            toValidate shouldBe expected.utbetalingsmetode
        }
    }

    fun validateUnderytelser(expected: UtbetalingEkstern, underytelser: List<Underytelse>) {
        underytelser.size shouldBe expected.ytelseListe.size

        val antallOriginal = expected.ytelseListe
            .flatMap { it.ytelseskomponentListe ?: emptyList() }
            .sumOf { it.satsantall ?: 0.0 }
        underytelser.sumOf { it.antall ?: 0.0 } shouldBeLessThanOrEqual antallOriginal

        val belopOriginal = expected.ytelseListe
            .flatMap { it.ytelseskomponentListe ?: emptyList() }
            .sumOf { it.satsbeloep ?: 0.0 }
        underytelser.sumOf { it.belop ?: 0.0 } shouldBeLessThanOrEqual belopOriginal
    }

    fun validateTrekk(expected: UtbetalingEkstern, trekk: List<Trekk>) {
        val skattOriginal = expected.ytelseListe
            .flatMap { it.skattListe ?: emptyList() }
            .sumOf { it.skattebeloep ?: 0.0 }

        val trekkOriginal = expected.ytelseListe
            .flatMap { it.trekkListe ?: emptyList() }
            .sumOf { it.trekkbeloep ?: 0.0 }

        trekk.sumOf { it.trekkBelop } shouldBeGreaterThanOrEqual skattOriginal + trekkOriginal
    }

}
