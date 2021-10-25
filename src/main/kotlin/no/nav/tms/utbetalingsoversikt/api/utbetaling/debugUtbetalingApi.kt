package no.nav.tms.utbetalingsoversikt.api.utbetaling

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.debugUtbetalingApi(utbetalingService: UtbetalingService) {

    get("/debug/utbetalinger") {
        val fromDate = call.request.fromDateParam
        val toDate = call.request.toDateParam

        val ident = call.request.userIdentCookie

        if (ident != null) {
            utbetalingService.fetchUtbetalingForPeriod (ident, fromDate, toDate).let { utbetaling ->
                call.respond(HttpStatusCode.OK, utbetaling)
            }
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Manglet ident-cookie. Bruk /debug/setUser?userIdent=<ident>")
        }
    }

    get("/debug/setUser") {
        val ident = call.request.userIdentParam

        if (ident != null) {
            call.response.cookies.append("debugUserIdent", ident, maxAge = 3600L)
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.BadRequest, "Manglet parameter 'userIdent'")
        }
    }
}

private val ApplicationRequest.fromDateParam: String? get() = queryParameters["fom"]
private val ApplicationRequest.toDateParam: String? get() = queryParameters["tom"]
private val ApplicationRequest.userIdentParam: String? get() = queryParameters["userIdent"]

private val ApplicationRequest.userIdentCookie: String? get() = cookies["debugUserIdent"]
