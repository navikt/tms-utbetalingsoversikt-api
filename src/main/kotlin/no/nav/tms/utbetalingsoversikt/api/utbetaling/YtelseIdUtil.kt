package no.nav.tms.utbetalingsoversikt.api.utbetaling

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseskomponentEkstern
import java.security.MessageDigest
import java.time.LocalDate

object YtelseIdUtil {
    private val log = KotlinLogging.logger { }

    private val md5 = MessageDigest.getInstance("MD5")

    fun calculateId(posteringsdato: String, ytelse: YtelseEkstern): String {
        val datePart = LocalDate.parse(posteringsdato)
            .toEpochDay()
            .toString(radix = 16)

        val contentPart = hashString(ytelse)

        return "$datePart-$contentPart"
    }

    fun hashString(ytelse: YtelseEkstern): String {

        val hashLowerHalf = concat(
            ytelse.bilagsnummer,
            ytelse.ytelsestype,
            ytelse.ytelseNettobeloep,
            ytelse.trekksum,
            ytelse.skattsum,
            *mapBeskrivelseToBelop(ytelse.ytelseskomponentListe)
        )
            .toByteArray()
            .let { md5.digest(it) }
            .also { md5.reset() }

        return hashLowerHalf.joinToString(separator = "") { byte -> "%02x".format(byte) }
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
            log.warn { "Klarte ikke pakke ut info fra ytelseId $id" }
            throw IllegalYtelseIdException("Invalid ytelseId $id")
        }
    }
}

class IllegalYtelseIdException(message: String): IllegalArgumentException(message)
class UtbetalingNotFoundException(val ytelseId: String, val details:String): IllegalArgumentException()
