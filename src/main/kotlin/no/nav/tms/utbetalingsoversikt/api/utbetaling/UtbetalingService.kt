package no.nav.tms.utbetalingsoversikt.api.utbetaling

import io.ktor.util.*
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUser
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseService
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseComparator
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import java.time.LocalDate

class UtbetalingService(private val hovedytelseService: HovedytelseService) {

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
        val (date, hash) = unmarshalId(ytelseId)

        return hovedytelseService.getHovedytelserBetaltTilBruker(user, date, date)
            .filter { it.isInPeriod(date, date) }
            .filter { it.hashCode() == hash }
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

    private fun unmarshalId(id: String?): Pair<LocalDate, Int> {
        try {
            val (datePart, hashPart) = id!!.decodeBase64String().split("-")

            val date = datePart.toLong().let { LocalDate.ofEpochDay(it) }

            val hash = hashPart.toInt()

            return date to hash

        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid ytelseId $id")
        }
    }
}
