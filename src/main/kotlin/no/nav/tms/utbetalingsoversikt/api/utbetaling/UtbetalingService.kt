package no.nav.tms.utbetalingsoversikt.api.utbetaling

import io.ktor.util.*
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUser
import no.nav.tms.utbetalingsoversikt.api.utbetaling.YtelseIdUtil.unmarshalId
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseService
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseComparator
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.math.log

class UtbetalingService(private val hovedytelseService: HovedytelseService) {

    private val log = LoggerFactory.getLogger(UtbetalingService::class.java)

    suspend fun fetchUtbetalingForPeriod(user: IdportenUser, fromDateString: String?, toDateString: String?): UtbetalingResponse {

        val fromDate = InputDateService.getFromDate(fromDateString)
        val adjustedFromDate = InputDateService.getEarlierFromDateWithinMaxBound(fromDate)
        val toDate = InputDateService.getToDate(toDateString)

        return hovedytelseService.getHovedytelserBetaltTilBruker(user, adjustedFromDate, toDate)
            .filter { it.isInPeriod(fromDate, toDate)}
            .sortedWith(HovedytelseComparator::compareYtelse)
            .let { createUtbetalingResponse(it) }
    }

    suspend fun fetchYtelse(user: IdportenUser, ytelseId: String?): Hovedytelse {
        val date = YtelseIdUtil.unmarshalDateFromId(ytelseId)

        log.info("Henter ytelse for id: $ytelseId, dato: $date")

        return hovedytelseService.getHovedytelserBetaltTilBruker(user, date, date)
            .also{ it.forEach { log.info("Id: ${it.id}") } }
            .filter { it.id == ytelseId }
            .first()
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
}
