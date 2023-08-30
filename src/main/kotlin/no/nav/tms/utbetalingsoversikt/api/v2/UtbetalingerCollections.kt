package no.nav.tms.utbetalingsoversikt.api.v2

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


@Serializable
data class UtbetalingerContainer(
    val neste: List<UtbetalingForYtelse>,
    val tidligere: List<UtbetalingerPrMåned>,
    val utbetalingerIPeriode: UtbetalingerIPeriode
) {
    companion object {
        fun fromSokosResponse(utbetalingEksternList: List<UtbetalingEkstern>) =
            utbetalingEksternList
                .groupBy { LocalDate.parse(it.utbetalingsdato ?: it.posteringsdato).isBefore(LocalDate.now()) }
                .let { grouped ->
                    UtbetalingerContainer(
                        neste = UtbetalingForYtelse.fromSokosResponse(grouped[after]).sortedBy { it.dato },
                        tidligere = UtbetalingerPrMåned.fromSokosRepsponse(grouped[before]),
                        utbetalingerIPeriode = UtbetalingerIPeriode.fromSokosResponse(grouped[before])

                    )
                }

        private const val after = false
        private const val before = true
    }
}

@Serializable
data class UtbetalingerPrMåned(val år: Int, val måned: Int, val utbetalinger: List<UtbetalingForYtelse>) {
    companion object {
        fun fromSokosRepsponse(sokosUtbetalinger: List<UtbetalingEkstern>?): List<UtbetalingerPrMåned> =
            sokosUtbetalinger
                ?.groupBy {
                    it.monthYearKey()
                }
                ?.map {
                    UtbetalingerPrMåned(
                        år = it.key.year,
                        måned = it.key.month,
                        utbetalinger = UtbetalingForYtelse
                            .fromSokosResponse(it.value)
                            .sortedByDescending { utbetaling -> utbetaling.dato }
                    )
                }
                ?: emptyList()

        private fun UtbetalingEkstern.monthYearKey() =
            LocalDate.parse(this.utbetalingsdato)
                .let { MonthYearKey(it.monthValue, it.year) }

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
            val ytelserWithBeløp =
                allYtelser
                    .groupBy { it.ytelsestype ?: "ukjent" }
                    .map { m ->
                        Ytelse(
                            m.key,
                            m.value.sumOf { it.ytelseNettobeloep.toBigDecimal() + it.trekksum.toBigDecimal() })
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
        encoder.encodeString(value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString())
    }

}