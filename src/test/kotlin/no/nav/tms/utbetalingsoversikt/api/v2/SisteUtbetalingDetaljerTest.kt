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


private fun sokoResponse(
    dateStr: String = "2023-08-24",
    nettobeløp: Double = 999.5,
    utbetalt: Boolean = true
): UtbetalingEkstern {
    val eksternAktør = AktoerEkstern(
        aktoertype = AktoertypeEkstern.PERSON,
        identOld = "88776611",
        identNew = null,
        navn = "Navn Navnesen",
    )


    return UtbetalingEkstern(
        utbetaltTil = eksternAktør,
        utbetalingsmetode = "Bankkontooverføring",
        utbetalingsstatus = "dummyverdi",
        posteringsdato = dateStr,
        forfallsdato = dateStr,
        utbetalingsdato = if (utbetalt) dateStr else null,
        utbetalingNettobeloep = nettobeløp,
        utbetalingsmelding = "En eller annen melding",
        utbetaltTilKonto = BankkontoEkstern(kontonummer = "9988776655443322", kontotype = "norsk bankkonto"),
        ytelseListe = listOf(
            eksternYtelse(eksternAktør, dateStr, (nettobeløp / 4), "AAP"),
            eksternYtelse(eksternAktør, dateStr, (nettobeløp / 4), "DAG"),
            eksternYtelse(eksternAktør, dateStr, (nettobeløp / 4), "FORELDRE"),
            eksternYtelse(eksternAktør, dateStr, (nettobeløp / 4), "SOMETHING"),

            )
    )
}


private fun eksternYtelse(
    aktoerEkstern: AktoerEkstern,
    fom: String,
    nettobeløp: Double,
    ytelsesType: String,
    tom: String? = null
) =
    YtelseEkstern(
        ytelsestype = ytelsesType,
        ytelsesperiode = PeriodeEkstern(
            fom = fom,
            tom = tom ?: fom
        ),
        ytelseNettobeloep = nettobeløp,
        rettighetshaver = aktoerEkstern,
        skattsum = nettobeløp * 0.2,
        trekksum = nettobeløp * 0.8,
        ytelseskomponentersum = 0.0,
        skattListe = listOf(),
        trekkListe = listOf(),
        ytelseskomponentListe = listOf(),
        bilagsnummer = null,
        refundertForOrg = null
    )