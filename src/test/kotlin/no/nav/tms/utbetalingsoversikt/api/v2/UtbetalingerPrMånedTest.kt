package no.nav.tms.utbetalingsoversikt.api.v2

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class UtbetalingerPeriodeTest {

    private val testYtelsesList = listOf(
        eksternYtelse(
            fom = LocalDate.now().minusDays(15).toString(),
            aktoerEkstern = eksternTestAktør,
            nettobeløp = 180.77,
            trekkbeløp = 19.23,
            ytelsesType = "Ytelse1"
        ),
        eksternYtelse(
            fom = LocalDate.now().minusDays(15).toString(),
            aktoerEkstern = eksternTestAktør,
            nettobeløp = 180.77,
            trekkbeløp = 19.23,
            ytelsesType = "Ytelse1"
        ),
        eksternYtelse(
            fom = LocalDate.now().minusDays(15).toString(),
            aktoerEkstern = eksternTestAktør,
            nettobeløp = 87.11,
            trekkbeløp = 13.7,
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
        val forventetBrutto = 500.81
        val forventetNetto = 448.65

        val testRepons = listOf(
            sokoTestResponse(
                nettobeløp = forventetNetto,
                ytelsesListe = listOf(
                    eksternYtelse(
                        fom = LocalDate.now().minusDays(15).toString(),
                        aktoerEkstern = eksternTestAktør,
                        nettobeløp = 180.77,
                        trekkbeløp = 19.23,
                        ytelsesType = "Ytelse1"
                    ),
                    eksternYtelse(
                        fom = LocalDate.now().minusDays(15).toString(),
                        aktoerEkstern = eksternTestAktør,
                        nettobeløp = 180.77,
                        trekkbeløp = 19.23,
                        ytelsesType = "Ytelse1"
                    ),
                    eksternYtelse(
                        fom = LocalDate.now().minusDays(15).toString(),
                        aktoerEkstern = eksternTestAktør,
                        nettobeløp = 87.11,
                        trekkbeløp = 13.7,
                        ytelsesType = "Ytelse2"
                    )
                )
            )
        )

        UtbetalingerIPeriode.fromSokosResponse(testRepons).apply {
            brutto shouldBe forventetBrutto.toBigDecimal()
            netto shouldBe forventetNetto.toBigDecimal()
            ytelser.find { it.ytelse == "Ytelse1" }.apply {
                require(this != null)
                beløp shouldEqualDoubleValue 400.00
            }
            ytelser.find { it.ytelse == "Ytelse2" }.apply {
                require(this != null)
                beløp shouldEqualDoubleValue 100.81
            }
        }
    }

    @Test
    fun `utbetalinger og trekk for flere perioder `() {
        val forventetBrutto = 1502.43
        val forventetNetto = 1345.95

        val testRepons = listOf(
            sokoTestResponse(
                nettobeløp = forventetNetto,
                ytelsesListe = testYtelsesList + testYtelsesList + testYtelsesList
            )
        )

        UtbetalingerIPeriode.fromSokosResponse(testRepons).apply {
            brutto shouldBe forventetBrutto.toBigDecimal()
            netto shouldBe forventetNetto.toBigDecimal()
            ytelser.find { it.ytelse == "Ytelse1" }.apply {
                require(this != null)
                beløp shouldEqualDoubleValue 1200.00
            }
            ytelser.find { it.ytelse == "Ytelse2" }.apply {
                require(this != null)
                beløp shouldEqualDoubleValue 302.43
            }
        }
    }
}

private infix fun BigDecimal.shouldEqualDoubleValue(d: Double) {
    withClue("expected: $this, acutal: $d") {
        this.compareTo(d.toBigDecimal()) shouldBe 0
    }
}
