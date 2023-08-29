package no.nav.tms.utbetalingsoversikt.api.v2

import com.fasterxml.jackson.databind.JsonNode
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.*
import org.amshove.kluent.shouldBeOnOrAfter
import org.amshove.kluent.shouldBeOnOrBefore
import java.time.LocalDate

internal fun eksternYtelse(
    aktoerEkstern: AktoerEkstern,
    fom: String,
    nettobeløp: Double,
    ytelsesType: String,
    tom: String? = null,
    trekkbeløp: Double = 200.0
) =
    YtelseEkstern(
        ytelsestype = ytelsesType,
        ytelsesperiode = PeriodeEkstern(
            fom = fom,
            tom = tom ?: fom
        ),
        ytelseNettobeloep = nettobeløp,
        rettighetshaver = aktoerEkstern,
        skattsum = trekkbeløp,
        trekksum = trekkbeløp,
        ytelseskomponentersum = 0.0,
        skattListe = listOf(),
        trekkListe = listOf(),
        ytelseskomponentListe = listOf(),
        bilagsnummer = null,
        refundertForOrg = null
    )


internal fun sokoTestResponse(date: LocalDate, utbetalt: Boolean = true) = sokoTestResponse(
    dateStr = date.toString(),
    utbetalt = utbetalt
)


val eksternTestAktør = AktoerEkstern(
    aktoertype = AktoertypeEkstern.PERSON,
    identOld = "88776611",
    identNew = null,
    navn = "Navn Navnesen",
)

internal fun sokoTestResponse(
    dateStr: String = "2023-08-24",
    nettobeløp: Double = 999.5,
    utbetalt: Boolean = true,
    ytelsesListe: List<YtelseEkstern>? = null
): UtbetalingEkstern {

    return UtbetalingEkstern(
        utbetaltTil = eksternTestAktør,
        utbetalingsmetode = "Bankkontooverføring",
        utbetalingsstatus = "dummyverdi",
        posteringsdato = dateStr,
        forfallsdato = dateStr,
        utbetalingsdato = if (utbetalt) dateStr else null,
        utbetalingNettobeloep = nettobeløp,
        utbetalingsmelding = "En eller annen melding",
        utbetaltTilKonto = BankkontoEkstern(kontonummer = "9988776655443322", kontotype = "norsk bankkonto"),
        ytelseListe = ytelsesListe ?: listOf(
            eksternYtelse(eksternTestAktør, dateStr, (nettobeløp / 4), "AAP"),
            eksternYtelse(eksternTestAktør, dateStr, (nettobeløp / 4), "DAG"),
            eksternYtelse(eksternTestAktør, dateStr, (nettobeløp / 4), "FORELDRE"),
            eksternYtelse(eksternTestAktør, dateStr, (nettobeløp / 4), "SOMETHING"),

            )
    )
}


internal fun List<JsonNode>.shouldBeInDescendingDateOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    map { LocalDate.parse(it["dato"].asText()) }.forEach { utbetalingsdato ->
        utbetalingsdato shouldBeOnOrBefore sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}

internal fun List<LocalDate>.shouldBeInDescendingOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    forEach { utbetalingsdato ->
        utbetalingsdato shouldBeOnOrBefore sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}


internal fun List<LocalDate>.shouldBeInAscendingOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    forEach { utbetalingsdato ->
        utbetalingsdato shouldBeOnOrAfter sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}

internal fun List<JsonNode>.shouldBeInAscendingDateOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    map { LocalDate.parse(it["dato"].asText()) }.forEach { utbetalingsdato ->
        utbetalingsdato shouldBeOnOrAfter sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}