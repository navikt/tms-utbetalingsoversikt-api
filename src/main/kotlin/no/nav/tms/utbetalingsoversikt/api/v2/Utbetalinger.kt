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
data class SisteUtbetalingDetaljer(val sisteUtbetaling: Int, val ytelse: List<String>) {
    companion object {
        fun fromSokosRepsonse(it: List<UtbetalingEkstern>): SisteUtbetalingDetaljer {
            return SisteUtbetalingDetaljer(sisteUtbetaling = 0, ytelse = listOf())
        }
    }

}

