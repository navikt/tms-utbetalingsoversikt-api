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

        get("/{ytelseId}") {

            val ytelseId = call.parameters["ytelseId"]?: throw IllegalYtelseIdException("Ytelseid kan ikke være null")
            val date = YtelseIdUtil.unmarshalDateFromId(ytelseId)
            val ytelseDetaljer = sokosUtbetalingConsumer.fetchUtbetalingsInfo(
                user = authenticatedUser,
                fom = date,
                tom = date
            ).let {
                if (it.isEmpty())
                    throw UtbetalingNotFoundException(ytelseId,"Utbetalingsapi returnerer tom liste")
                YtelseUtbetalingDetaljer.fromSokosReponse(it, ytelseId)
            }
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

val ApplicationCall.fromDateParam: String? get() = request.queryParameters["fom"]
val ApplicationCall.toDateParam: String? get() = request.queryParameters["tom"]