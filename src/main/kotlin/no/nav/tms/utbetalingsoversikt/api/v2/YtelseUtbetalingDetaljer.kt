@file:UseSerializers(LocalDateSerializer::class)

package no.nav.tms.utbetalingsoversikt.api.v2

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.utbetalingsoversikt.api.config.BigDecimalSerializer
import no.nav.tms.utbetalingsoversikt.api.config.KontonummerSerializer
import no.nav.tms.utbetalingsoversikt.api.config.LocalDateSerializer
import no.nav.tms.utbetalingsoversikt.api.utbetaling.YtelseIdUtil
import no.nav.tms.utbetalingsoversikt.api.utbetaling.UtbetalingNotFoundException
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer.TrekkTransformer
import java.math.BigDecimal
import java.time.LocalDate

@Serializable
class YtelseUtbetalingDetaljer private constructor(
    @Serializable(with = KontonummerSerializer::class)
    val kontonummer: String?,
    val ytelse: String,
    val erUtbetalt: Boolean,
    val ytelsePeriode: FomTom,
    val ytelseDato: LocalDate,
    val underytelse: List<UnderytelseDetaljer>,
    val trekk: List<Trekk>,
    val melding: String
) {
    @Serializable(with = BigDecimalSerializer::class)
    val bruttoUtbetalt = underytelse.sumOf { it.beløp }

    @Serializable(with = BigDecimalSerializer::class)
    val nettoUtbetalt = underytelse.sumOf { it.beløp } - trekk.sumOf { it.beløp }

    companion object {
        fun fromSokosReponse(ekstern: List<UtbetalingEkstern>, ytelseId: String) =
            ekstern.first().let { utbetalingEkstern ->
                val ytelseEkstern = utbetalingEkstern.ytelseListe
                    .firstOrNull { ytelseEkstern ->
                        YtelseIdUtil.calculateId(utbetalingEkstern.posteringsdato, ytelseEkstern) == ytelseId
                    } ?: throw UtbetalingNotFoundException(
                        ytelseId,
                        "Ikke funnet i ytelsesliste med posteringsdato ${utbetalingEkstern.posteringsdato}"
                    )
                YtelseUtbetalingDetaljer(
                    kontonummer = utbetalingEkstern.utbetaltTilKonto?.kontonummer,
                    ytelse = ytelseEkstern.ytelsestype ?: "Ukjent",
                    erUtbetalt = utbetalingEkstern.utbetalingsdato != null,
                    ytelsePeriode = FomTom(
                        fom = LocalDate.parse(ytelseEkstern.ytelsesperiode.fom),
                        tom = LocalDate.parse(ytelseEkstern.ytelsesperiode.tom)
                    ),
                    ytelseDato = LocalDate.parse(utbetalingEkstern.utbetalingsdato ?: utbetalingEkstern.forfallsdato),
                    underytelse = ytelseEkstern.ytelseskomponentListe?.map {
                        UnderytelseDetaljer(
                            beskrivelse = it.ytelseskomponenttype ?: "Ukjent",
                            sats = it.satsbeloep?.toBigDecimal() ?: BigDecimal(0),
                            antall = it.satsantall?.toBigDecimal() ?: BigDecimal(0),
                            beløp = it.ytelseskomponentbeloep?.toBigDecimal() ?: BigDecimal(0)
                        )
                    } ?: emptyList(),
                    trekk = skatteTrekk(ytelseEkstern) + ytterligeTrekk(ytelseEkstern),
                    melding = utbetalingEkstern.utbetalingsmelding ?: ""
                )
            }

        private fun skatteTrekk(ytelseEkstern: YtelseEkstern) =
            ytelseEkstern.skattListe
                ?.map { it.skattebeloep ?: 0.0 }
                ?.map {
                    Trekk(
                        type = if (it > 0.0) "Tilbakebetaling skattetrekk" else "Skattetrekk",
                        beløp = it.toBigDecimal()
                    )
                }
                ?: emptyList()

        private fun ytterligeTrekk(ytelseEkstern: YtelseEkstern) =
            ytelseEkstern.trekkListe?.map {
                Trekk(
                    type = it.trekktype ?: "Ukjent",
                    beløp = it.trekkbeloep?.toBigDecimal() ?: BigDecimal(0)
                )
            } ?: emptyList()
    }


}

@Serializable
class FomTom(
    val fom: LocalDate,
    val tom: LocalDate
)

@Serializable
class UnderytelseDetaljer(
    val beskrivelse: String,
    @Serializable(with = BigDecimalSerializer::class) val sats: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) val antall: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) val beløp: BigDecimal
)

@Serializable
class Trekk(
    val type: String,
    @Serializable(with = BigDecimalSerializer::class) val beløp: BigDecimal
)
