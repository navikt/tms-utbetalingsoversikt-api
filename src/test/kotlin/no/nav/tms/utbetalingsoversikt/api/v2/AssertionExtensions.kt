package no.nav.tms.utbetalingsoversikt.api.v2

import com.fasterxml.jackson.databind.JsonNode
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.date.shouldNotBeAfter
import io.kotest.matchers.date.shouldNotBeBefore
import io.ktor.client.statement.*
import java.time.LocalDate

internal fun List<JsonNode>.shouldBeInDescendingDateOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    map { LocalDate.parse(it["dato"].asText()) }.forEach { utbetalingsdato ->
        utbetalingsdato shouldNotBeAfter sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}

internal fun List<LocalDate>.shouldBeInDescendingOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    forEach { utbetalingsdato ->
        utbetalingsdato shouldNotBeAfter sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}


internal fun List<LocalDate>.shouldBeInAscendingOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    forEach { utbetalingsdato ->
        utbetalingsdato shouldNotBeBefore sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}

internal fun List<JsonNode>.shouldBeInAscendingDateOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    map { LocalDate.parse(it["dato"].asText()) }.forEach { utbetalingsdato ->
        utbetalingsdato shouldNotBeBefore sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}

internal suspend fun HttpResponse.assert(function: suspend HttpResponse.() -> Unit) {
    function()
}

internal fun List<JsonNode>.shouldBeInDescedingYearMonthOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    map { LocalDate.of(it["år"].asInt(), it["måned"].asInt(), 1) }.forEach { utbetalingsdato ->
        utbetalingsdato shouldBeBefore sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}
