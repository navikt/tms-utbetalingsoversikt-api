package no.nav.tms.utbetalingsoversikt.api.utbetaling

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object InputDateParser {

    private const val FROM_DATE_OFFSET_DAYS = 20L

    private val DEFAULT_BASE_FROM_DATE get() = threeDaysBeforeNow()
    private val EARLIEST_POSSIBLE_FROM_DATE get() = threeYearsBeforeNowAtFirstDay()
    private val DEFAULT_TO_DATE get() = LocalDate.now()

    private val INPUT_DATE_FORMATER = DateTimeFormatter.ofPattern("yyyyMMdd")


    fun getEffectiveFromDate(baseFromDate: String?): LocalDate {
        val baseDate = if (baseFromDate != null) {
            INPUT_DATE_FORMATER.parse(baseFromDate).let { LocalDate.from(it) }
        } else {
            DEFAULT_BASE_FROM_DATE
        }

        return getAdjustedFromDateWithinMaxBound(baseDate)
    }

    fun getToDate(toDate: String?): LocalDate {
        return if (toDate != null) {
            INPUT_DATE_FORMATER.parse(toDate).let { LocalDate.from(it) }
        } else {
            DEFAULT_TO_DATE
        }
    }

    private fun getAdjustedFromDateWithinMaxBound(fromDate: LocalDate): LocalDate {
        val adjustedDate = fromDate.minusDays(FROM_DATE_OFFSET_DAYS)

        return maxOf(adjustedDate, EARLIEST_POSSIBLE_FROM_DATE)
    }

    private fun threeDaysBeforeNow() = LocalDate.now().minusMonths(3)

    private fun threeYearsBeforeNowAtFirstDay() = LocalDate.now().minusYears(3).withDayOfYear(1)

}
