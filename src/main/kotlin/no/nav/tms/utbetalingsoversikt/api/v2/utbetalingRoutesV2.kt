package no.nav.tms.utbetalingsoversikt.api.v2

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.utbetalingsoversikt.api.config.authenticatedUser
import no.nav.tms.utbetalingsoversikt.api.utbetaling.IllegalYtelseIdException
import no.nav.tms.utbetalingsoversikt.api.utbetaling.YtelseIdUtil
import no.nav.tms.utbetalingsoversikt.api.utbetaling.UtbetalingNotFoundException
import no.nav.tms.utbetalingsoversikt.api.ytelse.SokosUtbetalingConsumer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Route.utbetalingRoutesV2(sokosUtbetalingConsumer: SokosUtbetalingConsumer) {

    route("utbetalinger") {

        get("/alle") {

            val utbetalinger = sokosUtbetalingConsumer.fetchUtbetalingsInfo(
                user = authenticatedUser,
                fom = call.fromDateParamAdjusted,
                tom = call.toDateParam
            )

            call.respond(HttpStatusCode.OK, UtbetalingerContainer.fromSokosResponse(utbetalinger))
        }

        get("/siste") {
            val sisteUtbetaling = sokosUtbetalingConsumer.fetchUtbetalingsInfo(
                user = authenticatedUser,
                fom = LocalDate.now().minusMonths(3),
                tom = LocalDate.now()
            )

            call.respond(HttpStatusCode.OK, SisteUtbetalingDetaljer.fromSokosRepsonse(sisteUtbetaling))
        }

        get("/{ytelseId}") {

            val ytelseId = call.parameters["ytelseId"] ?: throw IllegalYtelseIdException("Ytelseid kan ikke være null")
            val date = YtelseIdUtil.unmarshalDateFromId(ytelseId)

            val ytelseDetaljer = sokosUtbetalingConsumer.fetchUtbetalingsInfo(
                user = authenticatedUser,
                fom = date,
                tom = date
            ).takeIf {
                it.isNotEmpty()
            }?.let {
                YtelseUtbetalingDetaljer.fromSokosReponse(it, ytelseId)
            } ?: throw UtbetalingNotFoundException(ytelseId, "Utbetalingsapi returnerer tom liste")

            call.respond(HttpStatusCode.OK, ytelseDetaljer)
        }
    }
}


private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
private fun String?.localDateOrDefault(default: LocalDate = LocalDate.now()): LocalDate = this?.let {
    LocalDate.parse(
        this,
        formatter
    )
} ?: default

val ApplicationCall.fromDateParamAdjusted: LocalDate
    get() = request.queryParameters["fom"].localDateOrDefault(
        LocalDate.now().minusMonths(3)
    ).let { fromDate ->
        getEarlierFromDateWithinMaxBound(fromDate)
    }

private const val FROM_DATE_OFFSET_DAYS = 20L
private val EARLIEST_POSSIBLE_FROM_DATE get() = LocalDate.now().minusYears(3).withDayOfYear(1)
private fun getEarlierFromDateWithinMaxBound(fromDate: LocalDate): LocalDate {
    val adjustedDate = fromDate.minusDays(FROM_DATE_OFFSET_DAYS)
    return maxOf(adjustedDate, EARLIEST_POSSIBLE_FROM_DATE)
}

val ApplicationCall.toDateParam: LocalDate
    get() = request.queryParameters["tom"].localDateOrDefault()
