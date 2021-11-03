package no.nav.tms.utbetalingsoversikt.api.ytelse

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.RolleEkstern.UTBETALT_TIL
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer.HovedytelseTransformer
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import java.time.LocalDate

class HovedytelseService(private val consumer: SokosUtbetalingConsumer) {

    suspend fun getHovedytelserBetaltTilBruker(ident: String, fom: LocalDate, tom: LocalDate): List<Hovedytelse> {
        return consumer.fetchUtbetalingsInfo(ident, fom, tom, UTBETALT_TIL)
            .map(HovedytelseTransformer::toHovedYtelse)
            .flatten()
    }
}
