package no.nav.tms.utbetalingsoversikt.api.utbetaling

import no.nav.tms.token.support.idporten.user.IdportenUser
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseService
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseComparator
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import org.slf4j.LoggerFactory
import java.time.LocalDate

class UtbetalingService(private val hovedytelseService: HovedytelseService) {

    private val log = LoggerFactory.getLogger(UtbetalingService::class.java)

    suspend fun fetchUtbetalingForPeriod(user: IdportenUser, fromDateString: String?, toDateString: String?): UtbetalingResponse {

        val fromDate = InputDateService.getFromDate(fromDateString)
        val adjustedFromDate = InputDateService.getEarlierFromDateWithinMaxBound(fromDate)
        val toDate = InputDateService.getToDate(toDateString)

        val unfilteredHovedYtelser = hovedytelseService.getHovedytelserBetaltTilBruker(user, adjustedFromDate, toDate)

        val filteredAndSortedHovedytelser = unfilteredHovedYtelser.filter { it.isWithinPeriod(fromDate, toDate) }
            .sortedWith(HovedytelseComparator::compareYtelse)

        conditionallyLogFilteringResult(unfilteredHovedYtelser, filteredAndSortedHovedytelser)

        return createUtbetalingResponse(filteredAndSortedHovedytelser)
    }

    private fun conditionallyLogFilteringResult(unfiltered: List<Hovedytelse>, filtered: List<Hovedytelse>) {
        if (unfiltered.size != filtered.size) {
            log.info("Filtrerte bort ${unfiltered.size - filtered.size} hovedytelser som lå utenfor ønsket tidsrom.")
        }
    }

    private fun Hovedytelse.isWithinPeriod(fromDate: LocalDate, toDate: LocalDate): Boolean {
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
}
