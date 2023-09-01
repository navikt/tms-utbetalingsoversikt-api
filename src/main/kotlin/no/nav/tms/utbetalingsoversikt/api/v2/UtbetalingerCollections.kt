package no.nav.tms.utbetalingsoversikt.api.v2

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

val log = KotlinLogging.logger { }

@Serializable
data class UtbetalingerContainer(
    val neste: List<UtbetalingForYtelse>,
    val tidligere: List<TidligereUtbetalingerPrMåned>,
    val utbetalingerIPeriode: UtbetalingerIPeriode
) {
    companion object {
        fun fromSokosResponse(utbetalingEksternList: List<UtbetalingEkstern>) =
            utbetalingEksternList
                .groupBy {
                    val compareDate = LocalDate.parse(it.utbetalingsdato ?: it.forfallsdato)
                    val now = LocalDate.now()
                    compareDate.isBefore(now) || (compareDate.isEqual(now) && it.utbetalingsdato != null)
                }
                .let { grouped ->
                    UtbetalingerContainer(
                        neste = UtbetalingForYtelse.fromSokosResponse(grouped[false]).sortedBy { it.dato },
                        tidligere = TidligereUtbetalingerPrMåned.fromSokosRepsponse(grouped[true])
                            .sortedWith(compareByDescending<TidligereUtbetalingerPrMåned> { it.år }.thenByDescending { it.måned }),
                        utbetalingerIPeriode = UtbetalingerIPeriode.fromSokosResponse(grouped[true])

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
    @Serializable(with = BigDecimalSerializer::class) val brutto: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) val netto: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) val trekk: BigDecimal,
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
                        ytelserMap.value.sumOf { it.ytelseNettobeloep.toBigDecimal() + it.trekksum.toBigDecimal() })
                }

            return UtbetalingerIPeriode(
                harUtbetalinger = true,
                brutto = ytelserWithBeløp.sumOf { it.beløp },
                netto = allYtelser.sumOf { it.ytelseNettobeloep.toBigDecimal() },
                trekk = allYtelser.sumOf { it.trekksum.toBigDecimal() },
                ytelser = ytelserWithBeløp
            )
        }

        private fun emptyPeriode() =
            UtbetalingerIPeriode(false, 0.0.toBigDecimal(), 0.0.toBigDecimal(), 0.0.toBigDecimal(), emptyList())
    }

    @Serializable
    data class Ytelse(
        val ytelse: String,
        @Serializable(with = BigDecimalSerializer::class) val beløp: BigDecimal
    )
}

class BigDecimalSerializer : KSerializer<BigDecimal> {
    override fun deserialize(decoder: Decoder): BigDecimal {
        return decoder.decodeString().toBigDecimal()
    }

    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "no.nav.tms.utbetalingsoversikt.api.v2",
        kind = PrimitiveKind.FLOAT
    )

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeDouble(value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toDouble())
    }

}