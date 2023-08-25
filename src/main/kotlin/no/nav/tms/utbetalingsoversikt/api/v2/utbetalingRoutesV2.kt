package no.nav.tms.utbetalingsoversikt.api.v2

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.utbetalingsoversikt.api.config.authenticatedUser
import no.nav.tms.utbetalingsoversikt.api.ytelse.SokosUtbetalingConsumer
import java.time.LocalDate
import java.time.format.DateTimeFormatter


//https://www.intern.dev.nav.no/tms-utbetalingsoversikt-api/utbetalinger?=&fom=20230524&tom=20230824

fun Route.utbetalingRoutesV2(sokosUtbetalingConsumer: SokosUtbetalingConsumer) {

    route("utbetalinger") {

        get("/alle") {
            val utbetalinger = sokosUtbetalingConsumer.fetchUtbetalingsInfo(
                user = authenticatedUser,
                fom = call.fromDateParam.localDateOrDefault(LocalDate.now().minusMonths(3)),
                tom = call.toDateParam.localDateOrDefault()
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
    }
}


private val formatter = DateTimeFormatter.ofPattern("YYYYMMdd")
private fun String?.localDateOrDefault(default: LocalDate = LocalDate.now()): LocalDate = this?.let {
    LocalDate.parse(
        this,
        formatter
    )
} ?: default

val ApplicationCall.fromDateParam: String? get() = request.queryParameters["fom"]
val ApplicationCall.toDateParam: String? get() = request.queryParameters["tom"]