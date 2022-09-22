package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.SkattEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.TrekkEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Trekk

object TrekkTransformer {
    fun createTrekkList(ytelse: YtelseEkstern): List<Trekk> {

        val totalTrekk = createTrekkListeFromTrekk(ytelse) + createTrekkListeFromSkatt(ytelse)

        return totalTrekk.groupBy { it.trekkType }
            .values
            .map(TrekkTransformer::sumOfTrekk)
    }

    private fun createTrekkListeFromSkatt(ytelse: YtelseEkstern): List<Trekk> {
        return ytelse.skattListe
            ?.map(TrekkTransformer::toTrekk)
            ?: emptyList()
    }

    private fun toTrekk(skatt: SkattEkstern): Trekk {
        val trekkBeloep = skatt.nonNullBeloepOrZero

        return Trekk(
            trekkType = if (trekkBeloep > 0) "Tilbakebetaling skattetrekk" else "Skattetrekk",
            trekkBelop = trekkBeloep
        )
    }

    private fun createTrekkListeFromTrekk(ytelse: YtelseEkstern): List<Trekk> {
        return ytelse.trekkListe
            ?.map(TrekkTransformer::toTrekk)
            ?: emptyList()
    }

    private fun toTrekk(trekk: TrekkEkstern): Trekk {
        val trekkBeloep = trekk.nonNullBeloepOrZero

        val trekkType = if (trekk.trekktype != null) {
            addTilbakebetalingTextIfNecessary(trekkBeloep, trekk.trekktype)
        } else {
            addTilbakebetalingTextIfNecessary(trekkBeloep, "Trekk")
        }

        return Trekk(
            trekkType = trekkType,
            trekkBelop = trekkBeloep
        )
    }

    private fun sumOfTrekk(trekk: List<Trekk>): Trekk {
        return trekk.reduce { prev, curr ->
            Trekk(
                trekkType = curr.trekkType,
                trekkBelop = prev.trekkBelop + curr.trekkBelop,
            )
        }
    }

    private fun addTilbakebetalingTextIfNecessary(belop: Double, tekst: String): String {
        return if (belop > 0) {
            "Tilbakebetaling " + tekst.lowercase()
        } else {
            tekst
        }
    }

    private val TrekkEkstern.nonNullBeloepOrZero get() = this.trekkbeloep?: 0.0
    private val SkattEkstern.nonNullBeloepOrZero get() = this.skattebeloep?: 0.0
}
