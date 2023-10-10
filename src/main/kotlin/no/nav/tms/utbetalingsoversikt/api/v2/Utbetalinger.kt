package no.nav.tms.utbetalingsoversikt.api.v2

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.LocalDateSerializer
import no.nav.tms.utbetalingsoversikt.api.utbetaling.YtelseIdUtil
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseEkstern
import java.time.LocalDate

@Serializable
data class UtbetalingForYtelse(
    val id: String,
    val beløp: Double,
    @Serializable(with = LocalDateSerializer::class) val dato: LocalDate,
    val ytelse: String
) {
    companion object {
        private fun medGenerertId(ytelse: YtelseEkstern, dato: LocalDate, posteringsdato: String) = UtbetalingForYtelse(
            id = YtelseIdUtil.calculateId(posteringsdato, ytelse),
            beløp = ytelse.ytelseNettobeloep,
            dato = dato,
            ytelse = ytelse.ytelsestype ?: "Ukjent"
        )

        fun fromSokosResponse(utbetalingEkstern: List<UtbetalingEkstern>): List<UtbetalingForYtelse> =
            utbetalingEkstern
                .map { ytelseMappingObject ->
                    ytelseMappingObject.ytelseListe.map { mappedYtelse ->
                        medGenerertId(
                            ytelse = mappedYtelse,
                            dato = ytelseMappingObject.ytelsesdato()!!,
                            posteringsdato = ytelseMappingObject.posteringsdato
                        )
                    }
                }
                .flatten()
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

class UtbetalingSerializationException(message: String) : Exception(message)
