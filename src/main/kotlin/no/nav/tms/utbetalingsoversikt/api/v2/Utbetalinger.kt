package no.nav.tms.utbetalingsoversikt.api.v2

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.LocalDateSerializer
import no.nav.tms.utbetalingsoversikt.api.utbetaling.YtelseIdUtil
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseEkstern
import java.time.LocalDate

@Serializable
data class UtbetalingerContainer(val neste: List<UtbetalingForYtelse>, val tidligere: List<UtbetalingerPrMåned>) {
    companion object {
        fun fromSokosResponse(utbetalingEksternList: List<UtbetalingEkstern>) =
            utbetalingEksternList
                .groupBy { LocalDate.parse(it.utbetalingsdato ?: it.posteringsdato).isBefore(LocalDate.now()) }
                .let { grouped ->
                    UtbetalingerContainer(
                        neste = UtbetalingForYtelse.fromSokosResponse(grouped[after]).sortedBy { it.dato },
                        tidligere = UtbetalingerPrMåned.fromSokosRepsponse(grouped[before])
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
data class UtbetalingForYtelse(
    val id: String,
    val beløp: Double,
    @Serializable(with = LocalDateSerializer::class) val dato: LocalDate,
    val ytelse: String
) {
    companion object {
        fun medGenerertId(ytelse: YtelseEkstern, dato: LocalDate, posteringsdato: String) = UtbetalingForYtelse(
            id = YtelseIdUtil.calculateId(posteringsdato, ytelse),
            beløp = ytelse.ytelseNettobeloep,
            dato = dato,
            ytelse = ytelse.ytelsestype ?: "Ukjent"
        )

        fun fromSokosResponse(utbetalingEkstern: List<UtbetalingEkstern>?): List<UtbetalingForYtelse> =
            utbetalingEkstern
                ?.map {
                    UtbetalingForYtelseMappingObject(
                        LocalDate.parse(it.utbetalingsdato ?: it.posteringsdato),
                        it.posteringsdato,
                        it.ytelseListe
                    )
                }
                ?.map { ytelseMappingObject ->
                    ytelseMappingObject.ytelser.map { mappedYtelse ->
                        medGenerertId(
                            ytelse = mappedYtelse,
                            dato = ytelseMappingObject.utbetalingsDato,
                            posteringsdato = ytelseMappingObject.posteringsdato
                        )
                    }
                }
                ?.flatten()
                ?: emptyList()
    }
}

@Serializable
data class SisteUtbetalingDetaljer(
    @Serializable(with = LocalDateSerializer::class) val dato: LocalDate?,
    val sisteUtbetaling: Double?,
    val ytelser: Map<String, Double>,
    val harUtbetaling: Boolean
) {


    companion object {
        fun fromSokosRepsonse(sokosResponse: List<UtbetalingEkstern>): SisteUtbetalingDetaljer =
            sokosResponse
                .takeIf { it.isNotEmpty() }
                ?.let { eksterneUtbetalinger ->
                    eksterneUtbetalinger
                        .filter { it.utbetalingsdato != null }
                        .maxBy { it.utbetalingsdato.toLocalDate() }
                        .let { sisteUtbetaling ->
                            SisteUtbetalingDetaljer(
                                dato = sisteUtbetaling.utbetalingsdato.toLocalDate(),
                                sisteUtbetaling = sisteUtbetaling.utbetalingNettobeloep,
                                ytelser = sisteUtbetaling.ytelseListe.associate {
                                    (it.ytelsestype ?: "ukjent") to it.ytelseNettobeloep
                                },
                                harUtbetaling = true
                            )
                        }

                }
                ?: SisteUtbetalingDetaljer(
                    sisteUtbetaling = 0.0,
                    ytelser = mapOf(),
                    harUtbetaling = false,
                    dato = null
                )
    }
}

typealias Utbetalingsdato = String

private fun Utbetalingsdato?.toLocalDate(): LocalDate = try {
    LocalDate.parse(this)
} catch (e: Exception) {
    throw UtbetalingSerializationException("Fant ikke utbetalingsdato, ${e.message}")
}

private class UtbetalingForYtelseMappingObject(
    val utbetalingsDato: LocalDate,
    val posteringsdato: String,
    val ytelser: List<YtelseEkstern>
)

class UtbetalingSerializationException(message: String) : Exception(message)
