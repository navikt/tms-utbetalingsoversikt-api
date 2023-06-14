package no.nav.tms.utbetalingsoversikt.api.utbetaling

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseEkstern
import org.slf4j.LoggerFactory
import java.time.LocalDate

object YtelseIdUtil {
    private val log = LoggerFactory.getLogger(YtelseIdUtil::class.java)

    fun calculateId(utbetaling: UtbetalingEkstern, ytelse: YtelseEkstern): String {
        val datePart = LocalDate.parse(utbetaling.posteringsdato)
            .toEpochDay()
            .toString(radix = 16)

        val contentPart = ytelse.hashCode()
            .let(Integer::toHexString)

        return "$datePart-$contentPart"
    }

    fun unmarshalDateFromId(id: String?): LocalDate {
        try {
            val datePart = id!!.split("-").first()

            return datePart.toLong(16).let { LocalDate.ofEpochDay(it) }

        } catch (e: Exception) {
            log.warn("Klarte ikke pakke ut info fra ytelseId $id")

            throw IllegalArgumentException("Invalid ytelseId $id")
        }
    }
}
