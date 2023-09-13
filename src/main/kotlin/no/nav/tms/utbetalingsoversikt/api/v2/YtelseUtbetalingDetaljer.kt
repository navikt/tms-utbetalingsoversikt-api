package no.nav.tms.utbetalingsoversikt.api.v2

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import java.math.BigDecimal
import java.time.LocalDate

class YtelseUtbetalingDetaljer(
    kontonummerFraSokos: String?,
    val ytelse: String,
    val erUtbetalt: Boolean,
    val ytelsePeriode: FomTom,
    val ytselseDato: LocalDate,
    val underytelse: List<UnderytelseDetaljer>,
    val trekk: List<Trekk>,
    val melding: String
) {
    val kontonummer:String = kontonummerFraSokos?.let { "xxxxxx"+it.substring(it.length-3)  }?:"----"
    val bruttoUtbetalt = underytelse.sumOf { it.beløp }
    val nettoUtbetalts = underytelse.sumOf { it.beløp } - trekk.sumOf { it.beløp }

    companion object {
        fun fromSokosReponse(ekstern: List<UtbetalingEkstern>) = ekstern.first().let { utbetalingEkstern ->
            val ytelseEkstern = utbetalingEkstern.ytelseListe.first()
            YtelseUtbetalingDetaljer(
                kontonummerFraSokos = utbetalingEkstern.utbetaltTilKonto?.kontonummer,
                ytelse = ytelseEkstern.ytelsestype ?: "Ukjent",
                erUtbetalt = utbetalingEkstern.utbetalingsdato != null,
                ytelsePeriode = FomTom(
                    fom = LocalDate.parse(ytelseEkstern.ytelsesperiode.fom),
                    tom = LocalDate.parse(ytelseEkstern.ytelsesperiode.tom)
                ),
                ytselseDato = LocalDate.parse(utbetalingEkstern.utbetalingsdato ?: utbetalingEkstern.forfallsdato),
                underytelse = ytelseEkstern.ytelseskomponentListe?.map {
                    UnderytelseDetaljer(
                        beskrivelse = it.ytelseskomponenttype ?: "Ukjent",
                        sats = it.satsbeloep?.toBigDecimal() ?: BigDecimal(0),
                        antall = 0
                    )
                } ?: emptyList(),
                trekk = ytelseEkstern.trekkListe?.map {
                    Trekk(type = it.trekktype ?: "Ukjent", beløp = it.trekkbeloep?.toBigDecimal() ?: BigDecimal(0))
                } ?: emptyList(),
                melding = utbetalingEkstern.utbetalingsmelding?:""
            )
        }
    }


}
class FomTom(val fom: LocalDate, val tom: LocalDate)

class UnderytelseDetaljer(val beskrivelse: String, val sats: BigDecimal, val antall: Int) {
    val beløp: BigDecimal = sats * BigDecimal(antall)
}

class Trekk(val type: String, val beløp: BigDecimal)