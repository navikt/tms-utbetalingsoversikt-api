package no.nav.tms.utbetalingsoversikt.api.v2

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.utbetalingRoutesV2(){

    route("utbetalinger"){

        get("/alle") {
            call.respond(HttpStatusCode.NotImplemented)
        }

        get("/siste") {
            call.respond(HttpStatusCode.NotImplemented)
        }
    }
}