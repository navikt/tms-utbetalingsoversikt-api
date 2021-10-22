package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseskomponentEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Underytelse

object UnderytelseTransformer {
    fun createUnderytelser(ytelseskomponentListe: List<YtelseskomponentEkstern>): List<Underytelse> {
        val grupperteUnderytelser = ytelseskomponentListe
            .map(UnderytelseTransformer::toUnderytelse)
            .groupBy { it.beskrivelse?: "" }

        return grupperteUnderytelser.values
            .map(UnderytelseTransformer::sumOfUnderytelse)
            .sortedByDescending { it.belop }
    }

    private fun toUnderytelse(ytelseskomponent: YtelseskomponentEkstern): Underytelse {
        return Underytelse(
            ytelseskomponent.hashCode(),
            ytelseskomponent.ytelseskomponenttype,
            ytelseskomponent.satstype,
            ytelseskomponent.satsbeloep,
            ytelseskomponent.satsantall,
            ytelseskomponent.ytelseskomponentbeloep,
        )
    }

    private fun sumOfUnderytelse(grupperteUnderytelser: List<Underytelse>): Underytelse {
        return grupperteUnderytelser.reduce { prev, curr ->
                Underytelse(
                    curr.id,
                    curr.beskrivelse,
                    curr.satstype,
                    curr.sats,
                    sumOfAntall(prev, curr),
                    sumOfBelop(prev, curr)
                )
            }
    }

    private fun sumOfAntall(ytelse1: Underytelse, ytelse2: Underytelse): Int {
        return ytelse1.nonNullAntallOrZero + ytelse2.nonNullAntallOrZero
    }

    private fun sumOfBelop(ytelse1: Underytelse, ytelse2: Underytelse): Double {
        return ytelse1.nonNullBelopOrZero + ytelse2.nonNullBelopOrZero
    }

    private val Underytelse.nonNullAntallOrZero get() = this.antall ?: 0
    private val Underytelse.nonNullBelopOrZero get() = this.belop ?: 0.0
}
