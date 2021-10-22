package no.nav.tms.utbetalingsoversikt.api.ytelse

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer.HovedytelseTransformer
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import org.slf4j.LoggerFactory
import java.time.LocalDate

class HovedytelseService(private val consumer: SokosUtbetalingConsumer) {
    val log = LoggerFactory.getLogger(HovedytelseService::class.java)

    suspend fun getHovedytelserBetaltTilBruker(ident: String, fom: LocalDate, tom: LocalDate): List<Hovedytelse> {
        return consumer.fetchUtbetalingsInfo(ident, fom, tom)
            .map(HovedytelseTransformer::toHovedYtelse)
            .flatten()
            .filter { it.isUtbetaltTilBruker() }
    }

    private fun Hovedytelse.isUtbetaltTilBruker(): Boolean {
        return if (utbetaltTil == null || rettighetshaver.navn == null) {
            false
        } else {
            val utbetaltTilString = utbetaltTil.toLowerCase().trim()
            val rettighetshaverString = rettighetshaver.navn.toLowerCase().trim()

            val compare = utbetaltTilString == rettighetshaverString

            log.info("$utbetaltTilString == $rettighetshaverString = $compare")
            return compare
        }
    }
}
