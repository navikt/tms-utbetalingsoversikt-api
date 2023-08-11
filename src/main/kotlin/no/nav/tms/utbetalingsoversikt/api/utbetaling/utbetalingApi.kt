package no.nav.tms.utbetalingsoversikt.api.utbetaling

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUser
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUserFactory

fun Route.utbetalingApi(utbetalingService: UtbetalingService) {

    get("/utbetalinger") {
        val fromDate = call.request.fromDateParam
        val toDate = call.request.toDateParam

        utbetalingService.fetchUtbetalingForPeriod(authenticatedUser, fromDate, toDate).let { utbetaling ->
            call.respond(HttpStatusCode.OK, utbetaling)
        }
    }

    get("/utbetaling") {
        val ytelseId = call.request.ytelseId

        try {
            utbetalingService.fetchYtelse(authenticatedUser, ytelseId).let { utbetaling ->
                call.respond(HttpStatusCode.OK, utbetaling)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(status = HttpStatusCode.BadRequest, "Invalid ytelseId")
        }
    }
}

private val ApplicationRequest.fromDateParam: String? get() = queryParameters["fom"]
private val ApplicationRequest.toDateParam: String? get() = queryParameters["tom"]

private val ApplicationRequest.ytelseId: String? get() = queryParameters["ytelseId"]

private val PipelineContext<Unit, ApplicationCall>.authenticatedUser: IdportenUser
    get() = IdportenUserFactory.createIdportenUser(call)
