package no.nav.tms.utbetalingsoversikt.api.utbetaling

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.BankkontoEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class UtbetalingerPeriodeTest {

    private val testYtelsesList = listOf(
        eksternYtelse(
            fom = LocalDate.now().minusDays(15).toString(),
            aktoerEkstern = eksternTestAktør,
            nettobeløp = 170.77,
            trekkbeløp = -19.23,
            skattsum = -10.0,
            ytelsesType = "Ytelse1"
        ),
        eksternYtelse(
            fom = LocalDate.now().minusDays(15).toString(),
            aktoerEkstern = eksternTestAktør,
            nettobeløp = 170.77,
            skattsum = -10.0,
            trekkbeløp = -19.23,
            ytelsesType = "Ytelse1"
        ),
        eksternYtelse(
            fom = LocalDate.now().minusDays(15).toString(),
            aktoerEkstern = eksternTestAktør,
            nettobeløp = 77.11,
            trekkbeløp = -13.7,
            skattsum = 10.0,
            ytelsesType = "Ytelse2"
        )
    )

    @Test
    fun `tomt hvis det ikke finnes noen utbetalinger`() {

        UtbetalingerIPeriode.fromSokosResponse(null).apply {
            brutto shouldEqualDoubleValue 0.0
            netto shouldEqualDoubleValue 0.0
            ytelser shouldBe emptyList()
            harUtbetalinger shouldBe false
        }
        UtbetalingerIPeriode.fromSokosResponse(emptyList()).apply {
            brutto shouldEqualDoubleValue 0.0
            netto shouldEqualDoubleValue 0.0
            ytelser shouldBe emptyList()
            harUtbetalinger shouldBe false
        }
    }

    @Test
    fun `utbetalinger og trekk for 1 periode `() {
        val forventetNetto = 418.65
        val foventetTrekk = -62.16
        val forventetBrutto = 480.81


        val testRepons = listOf(
            sokoTestResponse(
                nettobeløp = forventetNetto,
                ytelsesListe = listOf(
                    eksternYtelse(
                        fom = LocalDate.now().minusDays(15).toString(),
                        aktoerEkstern = eksternTestAktør,
                        nettobeløp = 170.77,
                        trekkbeløp = -19.23,
                        skattsum = -10.0,
                        ytelsesType = "Ytelse1"
                    ),
                    eksternYtelse(
                        fom = LocalDate.now().minusDays(15).toString(),
                        aktoerEkstern = eksternTestAktør,
                        nettobeløp = 170.77,
                        skattsum = -10.0,
                        trekkbeløp = -19.23,
                        ytelsesType = "Ytelse1"
                    ),
                    eksternYtelse(
                        fom = LocalDate.now().minusDays(15).toString(),
                        aktoerEkstern = eksternTestAktør,
                        nettobeløp = 77.11,
                        trekkbeløp = -13.7,
                        skattsum = 10.0,
                        ytelsesType = "Ytelse2"
                    )
                )
            )
        )

        UtbetalingerIPeriode.fromSokosResponse(testRepons).apply {
            brutto shouldBe forventetBrutto.toBigDecimal()
            netto shouldBe forventetNetto.toBigDecimal()
            trekk shouldBe foventetTrekk.toBigDecimal()
            ytelser.find { it.ytelse == "Ytelse1" }.apply {
                require(this != null)
                beløp shouldEqualDoubleValue 400.00
            }
            ytelser.find { it.ytelse == "Ytelse2" }.apply {
                require(this != null)
                beløp shouldEqualDoubleValue 80.81
            }
        }
    }

    @Test
    fun `utbetalinger og trekk for flere perioder `() {

        val forventetNetto = 1255.95
        val foventetTrekk = -186.48
        val forventetBrutto = 1442.43

        val testRepons = listOf(
            sokoTestResponse(
                nettobeløp = forventetNetto,
                ytelsesListe = testYtelsesList + testYtelsesList + testYtelsesList
            )
        )

        UtbetalingerIPeriode.fromSokosResponse(testRepons).apply {
            brutto shouldBe forventetBrutto.toBigDecimal()
            netto shouldBe forventetNetto.toBigDecimal()
            trekk shouldBe foventetTrekk.toBigDecimal()
            ytelser.find { it.ytelse == "Ytelse1" }.apply {
                require(this != null)
                beløp shouldEqualDoubleValue 1200.00
            }
            ytelser.find { it.ytelse == "Ytelse2" }.apply {
                require(this != null)
                beløp shouldEqualDoubleValue 242.43
            }
        }
    }

    @Test
    fun `behandling av tilbakebetalinger`() {
        val utbetalingAAPBrutto = 12000.5
        val utbetalingAAPNetto = 10000.0
        val utbetalingAAPSkatt = -2000.5
        val tilbakebetalingsTrekk = 550.3

        val dateStr = "2023-10-10"

        val utbetalingPlussTilbakebetaling = listOf(
                UtbetalingEkstern(
                utbetaltTil = eksternTestAktør,
                utbetalingsmetode = "Bankkontooverføring",
                utbetalingsstatus = "dummyverdi",
                posteringsdato = dateStr,
                forfallsdato = dateStr,
                utbetalingsdato = dateStr,
                utbetalingNettobeloep = 1550.3,
                utbetalingsmelding = "En eller annen melding",
                utbetaltTilKonto = BankkontoEkstern(kontonummer = "9988776655443322", kontotype = "norsk bankkonto"),
                ytelseListe = listOf(
                    eksternYtelse(eksternTestAktør, dateStr, utbetalingAAPNetto, "AAP", skattsum = utbetalingAAPSkatt, trekkbeløp = 0.0),
                    eksternYtelse(eksternTestAktør, dateStr, tilbakebetalingsTrekk, "Skattetrekk", skattsum = tilbakebetalingsTrekk, trekkbeløp = 0.0),
                )
            )
        )

        UtbetalingerIPeriode.fromSokosResponse(utbetalingPlussTilbakebetaling).apply {
            brutto shouldBe utbetalingAAPBrutto.toBigDecimal()
            netto shouldBe (utbetalingAAPNetto + tilbakebetalingsTrekk).toBigDecimal()
            trekk shouldBe (utbetalingAAPSkatt + tilbakebetalingsTrekk).toBigDecimal()
            ytelser.find { it.ytelse == "AAP" }.apply {
                require(this != null)
                beløp shouldEqualDoubleValue utbetalingAAPBrutto
            }
            ytelser.find { it.ytelse == "Skattetrekk" }.apply {
                require(this == null)
            }
        }
    }
}

private infix fun BigDecimal.shouldEqualDoubleValue(d: Double) {
    withClue("expected: $d, acutal: $this") {
        this.compareTo(d.toBigDecimal()) shouldBe 0
    }
}
