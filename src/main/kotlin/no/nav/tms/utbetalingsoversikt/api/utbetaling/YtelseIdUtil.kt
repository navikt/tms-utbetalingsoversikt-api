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

    fun unmarshalId(id: String?): Pair<LocalDate, Int> {
        try {
            val (datePart, hashPart) = id!!.split("-")

            val date = datePart.toLong(16).let { LocalDate.ofEpochDay(it) }

            val hash = Integer.parseUnsignedInt(hashPart,16)

            return date to hash

        } catch (e: Exception) {
            log.warn("Klarte ikke pakke ut info fra ytelseId $id")

            throw IllegalArgumentException("Invalid ytelseId $id")
        }
    }
}
