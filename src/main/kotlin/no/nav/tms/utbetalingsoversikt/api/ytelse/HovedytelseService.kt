package no.nav.tms.utbetalingsoversikt.api.ytelse

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer.HovedytelseTransformer
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import java.time.LocalDate

class HovedytelseService(private val consumer: SokosUtbetalingConsumer) {
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
            utbetaltTil.toLowerCase().trim() == rettighetshaver.navn.toLowerCase().trim()
        }
    }
}
