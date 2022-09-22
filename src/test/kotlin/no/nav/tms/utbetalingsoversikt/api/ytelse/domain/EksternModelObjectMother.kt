package no.nav.tms.utbetalingsoversikt.api.ytelse.domain

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.*
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.AktoertypeEkstern.PERSON
import java.time.LocalDate

object EksternModelObjectMother {

    fun giveMeUtbetalingEkstern() = UtbetalingEkstern(
        utbetaltTil =  giveMeAktoerEkstern(),
        utbetalingsmetode = "metode",
        utbetalingsstatus = "status",
        posteringsdato = LocalDate.now().minusMonths(1).toString(),
        forfallsdato = LocalDate.now().toString(),
        utbetalingsdato = LocalDate.now().toString(),
        utbetalingNettobeloep = 10000.0,
        utbetalingsmelding = "melding",
        utbetaltTilKonto = giveMeBankkontoEkstern(),
        ytelseListe = listOf(giveMeYtelseEkstern(), giveMeYtelseEkstern())
    )

    fun giveMeYtelseEkstern() = YtelseEkstern(
        ytelsestype = "ytelseType",
        ytelsesperiode = giveMePeriode(),
        ytelseNettobeloep = 5000.0,
        rettighetshaver = giveMeAktoerEkstern(),
        skattsum = -200.0,
        trekksum = -300.0,
        ytelseskomponentersum = 4500.0,
        skattListe = giveMeSkatteListe(-50.0, -150.0),
        trekkListe = giveMeTrekkListe(-180.0, -50.0, -70.0),
        ytelseskomponentListe = listOf(
            giveMeYtelsesKomponentEkstern("entype", 2000.0, 3000.0),
            giveMeYtelsesKomponentEkstern("annenType", 8000.0, 7000.0)
        ),
        bilagsnummer = "bilagsnummer",
        refundertForOrg = giveMeAktoerEkstern(),
    )

    fun giveMeBankkontoEkstern(kontonummer: String = "123456 12345") = BankkontoEkstern(
        kontonummer = kontonummer,
        kontotype = "kontotype"
    )

    fun giveMePeriode() = PeriodeEkstern(
        fom = LocalDate.now().minusMonths(2).toString(),
        tom = LocalDate.now().toString()
    )

    fun giveMeAktoerEkstern() = AktoerEkstern(
        aktoertype = PERSON,
        identNew = "12345678912",
        navn = "Navn Navnesen"
    )

    fun giveMeSkattEkstern(beloep: Double = -600.0) = SkattEkstern(beloep)

    private fun giveMeSkatteListe(vararg beloep: Double) = beloep.map { giveMeSkattEkstern(it) }

    fun giveMeTrekkEkstern(beloep: Double = -700.0) = TrekkEkstern(
        trekktype = "trekk",
        trekkbeloep = beloep,
        kreditor = "kreditor"
    )

    fun giveMeTrekkListe(vararg beloep: Double) = beloep.map { giveMeTrekkEkstern(it) }

    fun giveMeYtelsesKomponentEkstern(komponenttype: String = "type", satsBeloep: Double = 6000.0, komponentbeloep: Double = 7000.0, antall: Double = 1.0) = YtelseskomponentEkstern(
        ytelseskomponenttype = komponenttype,
        satsbeloep = satsBeloep,
        satstype = "type",
        satsantall = antall,
        ytelseskomponentbeloep = komponentbeloep
    )
}
