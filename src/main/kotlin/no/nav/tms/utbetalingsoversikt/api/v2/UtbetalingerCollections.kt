@file:UseSerializers(BigDecimalSerializer::class)

package no.nav.tms.utbetalingsoversikt.api.v2

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.utbetalingsoversikt.api.config.BigDecimalSerializer
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseEkstern
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.math.abs

val log = KotlinLogging.logger { }

@Serializable
data class UtbetalingerContainer(
    val neste: List<UtbetalingForYtelse>,
    val tidligere: List<TidligereUtbetalingerPrMåned>,
    val utbetalingerIPeriode: UtbetalingerIPeriode
) {
    companion object {
        fun fromSokosResponse(utbetalingEksternList: List<UtbetalingEkstern>, requestedFomDate: LocalDate) =
            utbetalingEksternList
                .groupBy {
                    val compareDate = LocalDate.parse(it.utbetalingsdato ?: it.forfallsdato)
                    val now = LocalDate.now()
                    compareDate.isBefore(now) || (compareDate.isEqual(now) && it.utbetalingsdato != null)
                }
                .let { grouped ->
                    val tidligere =
                        grouped[true]?.filter { it.guaranteedYtelseDato().isAfter(requestedFomDate.minusDays(1)) }
                    UtbetalingerContainer(
                        neste = UtbetalingForYtelse.fromSokosResponse(grouped[false]).sortedBy { it.dato },
                        tidligere = TidligereUtbetalingerPrMåned.fromSokosRepsponse(tidligere)
                            .sortedWith(compareByDescending<TidligereUtbetalingerPrMåned> { it.år }.thenByDescending { it.måned }),
                        utbetalingerIPeriode = UtbetalingerIPeriode.fromSokosResponse(tidligere)
                    )
                }
    }
}

@Serializable
data class TidligereUtbetalingerPrMåned(val år: Int, val måned: Int, val utbetalinger: List<UtbetalingForYtelse>) {
    companion object {
        fun fromSokosRepsponse(sokosUtbetalinger: List<UtbetalingEkstern>?): List<TidligereUtbetalingerPrMåned> =
            sokosUtbetalinger
                ?.groupBy { it.monthYearKey() }
                ?.filter { it.key != null }
                ?.map {
                    TidligereUtbetalingerPrMåned(
                        år = it.key!!.year,
                        måned = it.key!!.month,
                        utbetalinger = UtbetalingForYtelse.fromSokosResponse(it.value)
                            .sortedByDescending { utbetaling -> utbetaling.dato }
                    )
                }
                ?: emptyList()

        private fun UtbetalingEkstern.monthYearKey(): MonthYearKey? =
            try {
                LocalDate.parse(this.utbetalingsdato)
                    .let { MonthYearKey(it.monthValue, it.year) }
            } catch (exception: Exception) {
                log.error { "Feil i sortering; utbetalingsdato er null, forfallsdato er $forfallsdato" }
                null
            }

        private data class MonthYearKey(val month: Int, val year: Int)
    }
}

@Serializable
data class UtbetalingerIPeriode(
    val harUtbetalinger: Boolean,
    val brutto: BigDecimal,
    val netto: BigDecimal,
    val trekk: BigDecimal,
    val ytelser: List<Ytelse>
) {

    companion object {
        fun fromSokosResponse(utbetalinger: List<UtbetalingEkstern>?): UtbetalingerIPeriode {
            if (utbetalinger.isNullOrEmpty()) {
                return emptyPeriode()
            }

            val allYtelser = utbetalinger
                .map { it.ytelseListe }
                .flatten()

            val ytelserWithBeløp = allYtelser
                .groupBy { it.ytelsestype ?: "ukjent" }
                .map { ytelserMap ->
                    Ytelse(
                        ytelserMap.key,
                        ytelserMap.value.sumOf { it.ytelseNettobeloep.toBigDecimal() - it.trekksum.toBigDecimal() - it.skattsum.toBigDecimal() })
                }
                .filter { (it.beløp > BigDecimal.ZERO || it.beløp < BigDecimal.ZERO) }

            return UtbetalingerIPeriode(
                harUtbetalinger = true,
                brutto = allYtelser.sumOf { it.ytelseskomponentersum.toBigDecimal() },
                netto = allYtelser.sumOf { it.ytelseNettobeloep.toBigDecimal() },
                trekk = allYtelser.sumOf { it.trekksum.toBigDecimal() } + allYtelser.sumOf { it.skattsum.toBigDecimal() },
                ytelser = ytelserWithBeløp
            )
        }

        private fun emptyPeriode() =
            UtbetalingerIPeriode(false, 0.0.toBigDecimal(), 0.0.toBigDecimal(), 0.0.toBigDecimal(), emptyList())
    }

    @Serializable
    data class Ytelse(
        val ytelse: String,
        val beløp: BigDecimal
    )
}
