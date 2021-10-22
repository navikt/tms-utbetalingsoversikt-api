package no.nav.tms.utbetalingsoversikt.api.utbetaling

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object InputDateService {

    private const val FROM_DATE_OFFSET_DAYS = 20L

    private val DEFAULT_FROM_DATE get() = threeMonthsBeforeNow()
    private val EARLIEST_POSSIBLE_FROM_DATE get() = threeYearsBeforeNowAtFirstDay()
    private val DEFAULT_TO_DATE get() = LocalDate.now()

    private val INPUT_DATE_FORMATER = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun getFromDate(fromDate: String?): LocalDate {
        return parseDateOrDefault(fromDate, DEFAULT_FROM_DATE)
    }

    fun getToDate(toDate: String?): LocalDate {
        return parseDateOrDefault(toDate, DEFAULT_TO_DATE)
    }

    fun getEarlierFromDateWithinMaxBound(fromDate: LocalDate): LocalDate {
        val adjustedDate = fromDate.minusDays(FROM_DATE_OFFSET_DAYS)

        return maxOf(adjustedDate, EARLIEST_POSSIBLE_FROM_DATE)
    }

    private fun parseDateOrDefault(dateString: String?, default: LocalDate): LocalDate {
        return if (dateString != null) {
            INPUT_DATE_FORMATER.parse(dateString).let { LocalDate.from(it) }
        } else {
            default
        }
    }

    private fun threeMonthsBeforeNow() = LocalDate.now().minusMonths(3)

    private fun threeYearsBeforeNowAtFirstDay() = LocalDate.now().minusYears(3).withDayOfYear(1)

}
