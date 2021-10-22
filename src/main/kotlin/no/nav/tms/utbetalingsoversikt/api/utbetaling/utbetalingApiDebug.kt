package no.nav.tms.utbetalingsoversikt.api.utbetaling

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.slf4j.LoggerFactory

fun Route.utbetalingApiDebug(utbetalingService: UtbetalingService) {

    val log = LoggerFactory.getLogger(Route::class.java)

    get("/debug/utbetalinger") {
        val fromDate = call.request.fromDateParam
        val toDate = call.request.toDateParam
        val ident = call.request.ident

        utbetalingService.fetchUtbetalingForPeriod (ident, fromDate, toDate).let { utbetaling ->
            log.info("utbetaling: $utbetaling")

            call.respond(HttpStatusCode.OK, utbetaling)
        }
    }
}

private val ApplicationRequest.ident: String get() = queryParameters["ident"]!!
private val ApplicationRequest.fromDateParam: String? get() = queryParameters["fom"]
private val ApplicationRequest.toDateParam: String? get() = queryParameters["tom"]
