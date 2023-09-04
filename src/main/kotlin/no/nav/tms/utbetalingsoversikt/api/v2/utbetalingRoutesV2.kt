package no.nav.tms.utbetalingsoversikt.api.v2

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.utbetalingsoversikt.api.config.authenticatedUser
import no.nav.tms.utbetalingsoversikt.api.utbetaling.YtelseIdUtil
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
            val date = YtelseIdUtil.unmarshalDateFromId(call.parameters["ytelseId"])
            val ytelseDetaljer = sokosUtbetalingConsumer.fetchUtbetalingsInfo(
                user = authenticatedUser,
                fom = date,
                tom = date
            ).let {
                YtelseUtbetalingDetaljer.fromSokosReponse(it)
            }
            call.respond(HttpStatusCode.OK,ytelseDetaljer)
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

/*{
  "ytelse": "Navn på ytelse",
  "erUtbetalt": "true/false",
  "ytelse_periode": {
    "fom": "dato",
    "tom": "dato"
  },
  "ytelse_dato": "utbetaltdato/forfallsdato ",
  "kontonummer": "xxxxxx9876",
  "underytelser": [
    {
      "beskrivelse": "Grunnbeløp",
      "sats": 100,
      "antall": "int eller 0",
      "__beløp_desc__": "samlet beløp(sats*antall)",
      "beløp": 300
    }
  ],
  "trekk": [
    {
      "type":"Skatt",
      "beløp": 100
    }
  ],
  "melding": "",
  "netto_utbetalt": "",
  "brutto_utbetalt": ""
}*/