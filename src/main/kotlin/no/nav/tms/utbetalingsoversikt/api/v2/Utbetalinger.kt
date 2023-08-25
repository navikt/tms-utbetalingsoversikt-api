package no.nav.tms.utbetalingsoversikt.api.v2

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.LocalDateSerializer
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import java.time.LocalDate

@Serializable
data class UtbetalingerContainer(val neste: List<UtbetalingsDetaljer>, val tidligere: List<UtbetalingerPrMåned>)

@Serializable
data class UtbetalingerPrMåned(val år: Int, val måned: Int, val utbetalinger: List<UtbetalingsDetaljer>)

@Serializable
data class UtbetalingsDetaljer(
    val id: String,
    val beløp: Int,
    @Serializable(with = LocalDateSerializer::class) val dato: LocalDate,
    val ytelse: String
) {
    companion object {
        fun medGenerertId(beløp: Int, dato: LocalDate, ytelse: String) = UtbetalingsDetaljer(
            id = genererId(),
            beløp = beløp,
            dato = dato,
            ytelse = ytelse
        )

        private fun genererId() = ""
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

private fun Utbetalingsdato?.toLocalDate(): LocalDate = this
    ?.let { LocalDate.parse(it) }
    ?: throw UtbetalingSerializationException("Fant ikke utbetalingsdato")


class UtbetalingSerializationException(message: String) : Exception(message)
