package no.nav.tms.utbetalingsoversikt.api.utbetaling

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseskomponentEkstern
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.time.LocalDate

object YtelseIdUtil {
    private val log = LoggerFactory.getLogger(YtelseIdUtil::class.java)

    private val md5 = MessageDigest.getInstance("MD5")

    fun calculateId(posteringsdato: String, ytelse: YtelseEkstern): String {
        val datePart = LocalDate.parse(posteringsdato)
            .toEpochDay()
            .toString(radix = 16)


        val contentPart = hashString(ytelse)

        log.info("Created id with contentPart $contentPart with datePart $datePart for $ytelse.")

        return "$datePart-$contentPart"
    }

    fun hashString(ytelse: YtelseEkstern): String {
        val hashLowerHalf = concat(
            ytelse.bilagsnummer,
            ytelse.ytelsestype,
            *mapBeskrivelseToBelop(ytelse.ytelseskomponentListe)
        )
            .toByteArray()
            .let { md5.digest(it) }
            .also { md5.reset() }
            .slice(0..7)

        return hashLowerHalf.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private fun YtelseEkstern.ytelseskomponentSum(): Double {
        return ytelseskomponentListe
            ?.sumOf { it.ytelseskomponentbeloep ?: 0.0 }
            ?: 0.0
    }

    private fun mapBeskrivelseToBelop(komponenter: List<YtelseskomponentEkstern>?): Array<String> {
        if (komponenter == null) {
            return emptyArray()
        }

        return komponenter.sortedWith(
            compareBy({it.ytelseskomponenttype ?: "" }, {it.ytelseskomponentbeloep ?: 0.0})
        )
            .map { "${it.ytelseskomponenttype}:${it.ytelseskomponentbeloep}" }
            .toTypedArray()
    }

    private fun concat(vararg params: Any?) = params.joinToString("_") { it.toString() }

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
