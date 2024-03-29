package no.nav.tms.utbetalingsoversikt.api.utbetaling

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUser
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseService
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseComparator
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import java.time.LocalDate

class UtbetalingService(private val hovedytelseService: HovedytelseService) {

    private val log = KotlinLogging.logger {}
    private val secureLog = KotlinLogging.logger("secureLog")

    suspend fun fetchUtbetalingForPeriod(user: IdportenUser, fromDateString: String?, toDateString: String?): UtbetalingResponse {

        val fromDate = InputDateService.getFromDate(fromDateString)
        val adjustedFromDate = InputDateService.getEarlierFromDateWithinMaxBound(fromDate)
        val toDate = InputDateService.getToDate(toDateString)

        return hovedytelseService.getHovedytelserBetaltTilBruker(user, adjustedFromDate, toDate)
            .also { debugLogAvvikMottaker(it, user) }
            .filter { it.isInPeriod(fromDate, toDate)}
            .sortedWith(HovedytelseComparator::compareYtelse)
            .let { createUtbetalingResponse(it) }
    }

    suspend fun fetchYtelse(user: IdportenUser, ytelseId: String?): Hovedytelse {
        val date = YtelseIdUtil.unmarshalDateFromId(ytelseId)

        log.info { "Henter ytelse for id: $ytelseId, dato: $date" }

        return hovedytelseService.getHovedytelserBetaltTilBruker(user, date, date)
            .first { it.id == ytelseId }
    }

    private fun Hovedytelse.isInPeriod(fromDate: LocalDate, toDate: LocalDate): Boolean {
        return when {
            ytelseDato != null && erUtbetalt -> ytelseDato in fromDate..toDate
            ytelseDato == null -> false
            else -> true
        }
    }

    private fun createUtbetalingResponse(hovedytelser: List<Hovedytelse>): UtbetalingResponse {
        val (utbetalte, kommende) = hovedytelser.partition { it.erUtbetalt }

        val rettighetshaver = hovedytelser.map { it.rettighetshaver }.firstOrNull()

        return UtbetalingResponse(rettighetshaver, utbetalte, kommende)
    }

    private fun debugLogAvvikMottaker(hovedytelser: List<Hovedytelse>, user: IdportenUser) {
        hovedytelser
            .filter { it.rettighetshaver.aktoerId != user.ident }
            .map { avvik ->
                secureLog.info { "Betaling (${avvik.melding}, ${avvik.ytelse}) har annen mottaker (${avvik.rettighetshaver.aktoerId}) enn innlogget bruker (${user.ident})" }
            }
    }

}
