package no.nav.tms.utbetalingsoversikt.api.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.healthApi() {
    get("/internal/ping") {
        call.respondText("""{"ping": "pong"}""", ContentType.Application.Json)
    }

    get("/internal/isAlive") {
        call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain)
    }

    get("/internal/isReady") {
            call.respondText(text = "READY", contentType = ContentType.Text.Plain)
    }

    get("/internal/selftest") {
        call.respond(HttpStatusCode.OK)
    }
}
