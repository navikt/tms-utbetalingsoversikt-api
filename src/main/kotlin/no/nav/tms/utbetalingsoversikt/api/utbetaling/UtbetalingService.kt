package no.nav.tms.utbetalingsoversikt.api.utbetaling

import no.nav.tms.token.support.idporten.user.IdportenUser
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseService
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseComparator
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import org.slf4j.LoggerFactory
import java.time.LocalDate

class UtbetalingService(private val hovedytelseService: HovedytelseService) {

    val log = LoggerFactory.getLogger(UtbetalingService::class.java)

    suspend fun fetchUtbetalingForPeriod(ident: String, fromDateString: String?, toDateString: String?) {

        val fromDate = InputDateParser.getEffectiveFromDate(fromDateString)
        val toDate = InputDateParser.getToDate(toDateString)

        return hovedytelseService.getHovedytelserBetaltTilBruker(ident, fromDate, toDate)
            .filter { it.isInPeriod(fromDate, toDate)}
            .sortedWith(HovedytelseComparator::compareYtelse)
            .let { createUtbetalingResponse(it) }
    }

    private fun Hovedytelse.isInPeriod(fromDate: LocalDate, toDate: LocalDate): Boolean {

        val isInPeriod = when {
            ytelseDato != null && erUtbetalt -> ytelseDato in fromDate..toDate
            ytelseDato == null -> false
            else -> true
        }

        log.info("{ytelseDato: $ytelseDato, fromDate: $fromDate, toDate: $toDate, isInPeriod: $isInPeriod} ")

        return isInPeriod
    }

    private fun createUtbetalingResponse(hovedytelser: List<Hovedytelse>): UtbetalingResponse {
        val (utbetalte, kommende) = hovedytelser.partition { it.erUtbetalt }

        val rettighetshaver = hovedytelser.map { it.rettighetshaver }.firstOrNull()

        return UtbetalingResponse(rettighetshaver, utbetalte, kommende)
    }
}
