package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.SkattEsktern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.TrekkEsktern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Trekk
import java.math.BigDecimal
import java.math.BigDecimal.ZERO

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

    private fun toTrekk(skatt: SkattEsktern): Trekk {
        val trekkBeloep = skatt.nonNullBeloepOrZero

        return Trekk(
            id = skatt.hashCode(),
            trekkType = if (trekkBeloep > ZERO) "Tilbakebetaling skattetrekk" else "Skattetrekk",
            trekkBelop = trekkBeloep
        )
    }

    private fun createTrekkListeFromTrekk(ytelse: YtelseEkstern): List<Trekk> {
        return ytelse.trekkListe
            ?.map(TrekkTransformer::toTrekk)
            ?: emptyList()
    }

    private fun toTrekk(trekk: TrekkEsktern): Trekk {
        val trekkBeloep = trekk.nonNullBeloepOrZero

        val trekkType = if (trekk.trekktype != null) {
            addTilbakebetalingTextIfNecessary(trekkBeloep, trekk.trekktype)
        } else {
            addTilbakebetalingTextIfNecessary(trekkBeloep, "Trekk")
        }

        return Trekk(
            id = trekk.hashCode(),
            trekkType = trekkType,
            trekkBelop = trekkBeloep
        )
    }

    private fun sumOfTrekk(trekk: List<Trekk>): Trekk {
        return trekk.reduce { prev, curr ->
            Trekk(
                id = curr.id,
                trekkType = curr.trekkType,
                trekkBelop = prev.trekkBelop + curr.trekkBelop,
            )
        }
    }

    private fun addTilbakebetalingTextIfNecessary(belop: BigDecimal, tekst: String): String {
        return if (belop > ZERO) "Tilbakebetaling " + tekst.toLowerCase() else tekst
    }

    private val TrekkEsktern.nonNullBeloepOrZero get() = this.trekkbeloep?: ZERO
    private val SkattEsktern.nonNullBeloepOrZero get() = this.skattebeloep?: ZERO
}
