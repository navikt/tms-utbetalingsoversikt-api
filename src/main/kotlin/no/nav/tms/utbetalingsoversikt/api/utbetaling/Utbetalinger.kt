package no.nav.tms.utbetalingsoversikt.api.utbetaling

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.LocalDateSerializer
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern.Companion.nesteUtbetaling
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern.Companion.sisteUtbetaling
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
data class SisteOgNesteUtbetaling(
    val hasUtbetaling: Boolean,
    val hasKommende: Boolean,
    val sisteUtbetaling: UtbetalingOppsummering?,
    val kommende: UtbetalingOppsummering?
) {
    companion object {

        fun fromSokosResponse(utbetalinger: List<UtbetalingEkstern>): SisteOgNesteUtbetaling {
            val now: LocalDate = LocalDate.now()
            val siste = utbetalinger.sisteUtbetaling(now)
            val nesteUtbetaling = utbetalinger.nesteUtbetaling(now)

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
                        dato = utbetalingEkstern.ytelsesdato()!!
                    )
                },
                kommende = nesteUtbetaling?.let { utbetalingEkstern ->
                    val ytelse = utbetalingEkstern.ytelseListe.first()
                    UtbetalingOppsummering(
                        id = YtelseIdUtil.calculateId(utbetalingEkstern.posteringsdato, ytelse),
                        utbetaling = ytelse.ytelseNettobeloep,
                        kontonummer = utbetalingEkstern.maskertKontonummer(),
                        ytelse = ytelse.ytelsestype ?: "Diverse",
                        dato = utbetalingEkstern.ytelsesdato()!!
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
