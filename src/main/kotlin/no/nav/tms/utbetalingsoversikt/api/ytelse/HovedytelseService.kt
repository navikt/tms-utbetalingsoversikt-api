package no.nav.tms.utbetalingsoversikt.api.ytelse

import no.nav.tms.token.support.idporten.user.IdportenUser
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.RolleEkstern.UTBETALT_TIL
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer.HovedytelseTransformer
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import java.time.LocalDate

class HovedytelseService(private val consumer: SokosUtbetalingConsumer) {

    suspend fun getHovedytelserBetaltTilBruker(user: IdportenUser, fom: LocalDate, tom: LocalDate): List<Hovedytelse> {
        return consumer.fetchUtbetalingsInfo(user, fom, tom, UTBETALT_TIL)
            .map(HovedytelseTransformer::toHovedYtelse)
            .flatten()
    }
}
