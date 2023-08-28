package no.nav.tms.utbetalingsoversikt.api.v2

import io.kotest.matchers.shouldBe
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SisteUtbetalingDetaljerTest {

    @Test
    fun `transformerer tom response`() {
        SisteUtbetalingDetaljer.fromSokosRepsonse(emptyList()).apply {
            harUtbetaling shouldBe false
            ytelser shouldBe emptyMap()
            sisteUtbetaling shouldBe 0
        }
    }

    @Test
    fun `transformerer respons med innhold`() {
        val forventetNettoDelbeløp = 999.5 / 4

        SisteUtbetalingDetaljer.fromSokosRepsonse(
            listOf(
                sokoResponse(dateStr = "2023-08-24", nettobeløp = 999.5),
                sokoResponse(dateStr = "2023-08-13", nettobeløp = 8700.0)
            )
        ).apply {
            dato shouldBe LocalDate.of(2023, 8, 24)
            harUtbetaling shouldBe true
            sisteUtbetaling shouldBe 999.5
            ytelser.size shouldBe 4
            //TODO: Finn ut av mapping
            ytelser["AAP"] shouldBe forventetNettoDelbeløp
            ytelser["DAG"] shouldBe forventetNettoDelbeløp
            ytelser["FORELDRE"] shouldBe forventetNettoDelbeløp
            ytelser["SOMETHING"] shouldBe forventetNettoDelbeløp
        }
    }

    @Test
    fun `transformerer respons med innhold hvor en av utbetlingene ikke har utbetalingsdato`() {
        val forventetNettoDelbeløp = 8700.0 / 4

        SisteUtbetalingDetaljer.fromSokosRepsonse(
            listOf(
                sokoResponse(dateStr = "2023-08-24", nettobeløp = 999.5, utbetalt = false),
                sokoResponse(dateStr = "2023-08-13", nettobeløp = 8700.0, utbetalt = true)
            )
        ).apply {
            dato shouldBe LocalDate.of(2023, 8, 13)
            harUtbetaling shouldBe true
            sisteUtbetaling shouldBe 8700.0
            ytelser.size shouldBe 4
            //TODO: Finn ut av mapping
            ytelser["AAP"] shouldBe forventetNettoDelbeløp
            ytelser["DAG"] shouldBe forventetNettoDelbeløp
            ytelser["FORELDRE"] shouldBe forventetNettoDelbeløp
            ytelser["SOMETHING"] shouldBe forventetNettoDelbeløp
        }
    }
}


