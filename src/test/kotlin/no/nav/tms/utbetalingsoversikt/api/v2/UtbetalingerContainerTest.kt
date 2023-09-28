package no.nav.tms.utbetalingsoversikt.api.v2

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UtbetalingerContainerTest {

    @Test
    fun `transformerer tomme resposone`() {
        UtbetalingerContainer.fromSokosResponse(emptyList(), LocalDate.now(), LocalDate.now()).apply {
            neste shouldBe emptyList()
            tidligere shouldBe emptyList()
        }
    }

    @Test
    fun `gruppperer riktig etter dato og utbetalt`() {
        val now = LocalDate.now()
        val threeMonthsBefore = now.minusMonths(3)
        val fourMonthsBefore = now.minusMonths(4)
        val sokoResponse = listOf(
            sokoTestResponse(date = now, false),
            sokoTestResponse(date = now.plusDays(6), false),
            sokoTestResponse(date = now, true),
            sokoTestResponse(date = now.firstInMonth()),
            sokoTestResponse(date = now.minus10or1stInMonth()),
            sokoTestResponse(date = threeMonthsBefore),
            sokoTestResponse(date = fourMonthsBefore),
            sokoTestResponse(date = now.minusYears(2)),
            sokoTestResponse(date = now.minusYears(2)),
        )

        UtbetalingerContainer.fromSokosResponse(sokoResponse, LocalDate.now().minusYears(5), LocalDate.now().plusYears(5)).apply {
            neste.groupBy { it.dato }.size shouldBe 2
            neste.map { it.dato }.shouldBeInAscendingOrder()
            tidligere.size shouldBe 4
            tidligere.find { it.år == now.year && it.måned == now.monthValue }.apply {
                require(this != null)
                utbetalinger.map { it.dato }.shouldBeInDescendingOrder()
            }

            tidligere.find { it.år == now.year - 2 && it.måned == now.monthValue }.apply {
                require(this != null)
                utbetalinger.size shouldBe 8
            }

            tidligere.find { it.år == threeMonthsBefore.year && it.måned == threeMonthsBefore.monthValue } shouldNotBe null
            tidligere.find { it.år == fourMonthsBefore.year && it.måned == fourMonthsBefore.monthValue } shouldNotBe null
        }
    }
}

private fun LocalDate.minus10or1stInMonth(): LocalDate = let {
    if (it.dayOfMonth - 10 >= 1) {
        LocalDate.now().minusDays(10)
    } else it.firstInMonth()
}

private fun LocalDate.firstInMonth(): LocalDate = LocalDate.of(year, monthValue, 1)
