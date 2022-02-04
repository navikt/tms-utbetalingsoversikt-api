package no.nav.tms.utbetalingsoversikt.api.ytelse

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import java.time.LocalDate

object HovedytelseComparator {
    fun compareYtelse(ytelse1: Hovedytelse, ytelse2: Hovedytelse): Int {
        val datoComparison = compareDatoNewFirstNullLast(ytelse1.ytelseDato, ytelse2.ytelseDato)

        return if (datoComparison != 0) {
            datoComparison
        } else {
            compareYtelsestype(ytelse1.ytelse, ytelse2.ytelse)
        }
    }

    private fun compareDatoNewFirstNullLast(dato1: LocalDate?, dato2: LocalDate?): Int {
        return when {
            dato1 == null && dato2 == null -> 0
            dato1 == null -> 1
            dato2 == null -> -1
            else -> dato1.compareTo(dato2).unaryMinus()
        }
    }

    private fun compareYtelsestype(ytelsestype1: String, ytelsestype2: String): Int {
        return ytelsestype1.lowercase().compareTo(ytelsestype2.lowercase())
    }
}
