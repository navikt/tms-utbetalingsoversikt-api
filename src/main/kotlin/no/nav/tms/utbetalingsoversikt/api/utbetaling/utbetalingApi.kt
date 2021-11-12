package no.nav.tms.utbetalingsoversikt.api.utbetaling

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import no.nav.tms.token.support.idporten.user.IdportenUser
import no.nav.tms.token.support.idporten.user.IdportenUserFactory
import no.nav.tms.utbetalingsoversikt.api.config.TokendingsTokenFetcher

fun Route.utbetalingApi(utbetalingService: UtbetalingService, tokenFetcher: TokendingsTokenFetcher) {

    get("/utbetalinger") {
        val fromDate = call.request.fromDateParam
        val toDate = call.request.toDateParam

        utbetalingService.fetchUtbetalingForPeriod(authenticatedUser, fromDate, toDate).let { utbetaling ->
            call.respond(HttpStatusCode.OK, utbetaling)
        }
    }

    get("/tokenx") {
        val token = tokenFetcher.getSokosUtbetaldataToken(authenticatedUser.tokenString)

        call.respond(HttpStatusCode.OK, token)
    }
}

private val ApplicationRequest.fromDateParam: String? get() = queryParameters["fom"]
private val ApplicationRequest.toDateParam: String? get() = queryParameters["tom"]

private val PipelineContext<Unit, ApplicationCall>.authenticatedUser: IdportenUser
    get() = IdportenUserFactory.createIdportenUser(call)
