package no.nav.tms.utbetalingsoversikt.api.v2

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.LocalDateSerializer
import no.nav.tms.utbetalingsoversikt.api.utbetaling.YtelseIdUtil
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern.Companion.nesteUtbetaling
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern.Companion.sisteUtbetaling
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern.Companion.toLocalDate
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

@Serializable
data class SisteOgNesteUtbetaling(
    val hasUtbetaling: Boolean,
    val hasKommende: Boolean,
    val sisteUtbetaling: UtbetalingOppsummering?,
    val kommende: UtbetalingOppsummering?
) {
    companion object {

        fun fromSokosResponse(utbetalinger: List<UtbetalingEkstern>): SisteOgNesteUtbetaling {
            val siste = utbetalinger.sisteUtbetaling()
            val nesteUtbetaling = utbetalinger.nesteUtbetaling()

            return SisteOgNesteUtbetaling(
                hasUtbetaling = siste != null,
                hasKommende = nesteUtbetaling != null,
                sisteUtbetaling = siste?.let { utbetalingEkstern ->
                    val ytelse = utbetalingEkstern.ytelseListe.first()
                    UtbetalingOppsummering(
                        id = YtelseIdUtil.calculateId(utbetalingEkstern.posteringsdato, ytelse),
                        utbetaling = ytelse.ytelseNettobeloep,
                        kontonummer = utbetalingEkstern.maskertKontonummer(),
                        ytelse = ytelse.ytelsestype ?: "Diverse",
                        dato = LocalDate.parse(utbetalingEkstern.utbetalingsdato)
                    )
                },
                kommende = nesteUtbetaling?.let { utbetalingEkstern ->
                    val ytelse = utbetalingEkstern.ytelseListe.first()
                    UtbetalingOppsummering(
                        id = YtelseIdUtil.calculateId(utbetalingEkstern.posteringsdato, ytelse),
                        utbetaling = ytelse.ytelseNettobeloep,
                        kontonummer = utbetalingEkstern.maskertKontonummer(),
                        ytelse = ytelse.ytelsestype ?: "Diverse",
                        dato = LocalDate.parse(utbetalingEkstern.forfallsdato)
                    )
                }
            )
        }
    }
}

@Serializable
class UtbetalingOppsummering(
    @Serializable(with = LocalDateSerializer::class) val dato: LocalDate,
    val id: String,
    val utbetaling: Double,
    val ytelse: String,
    val kontonummer: String, //fiks maskering!
)

class UtbetalingSerializationException(message: String) : Exception(message)
