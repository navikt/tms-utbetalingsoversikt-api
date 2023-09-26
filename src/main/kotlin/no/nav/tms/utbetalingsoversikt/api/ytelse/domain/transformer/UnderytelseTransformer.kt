package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.YtelseskomponentEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Underytelse

object UnderytelseTransformer {
    fun createUnderytelser(ytelseskomponentListe: List<YtelseskomponentEkstern>): List<Underytelse> {
        val grupperteUnderytelser = ytelseskomponentListe
            .map {
                Underytelse(
                    beskrivelse = it.ytelseskomponenttype,
                    satstype = it.satstype,
                    sats = it.satsbeloep,
                    antall = it.satsantall,
                    belop = it.ytelseskomponentbeloep,
                )
            }
            .groupBy { it.beskrivelse ?: "" }
            .values

        return grupperteUnderytelser
            .map(UnderytelseTransformer::sumOfUnderytelse)
            .sortedByDescending { it.belop }
    }

    private fun sumOfUnderytelse(grupperteUnderytelser: List<Underytelse>): Underytelse =
        grupperteUnderytelser.reduce { prev, curr ->
            Underytelse(
                beskrivelse = curr.beskrivelse,
                satstype = curr.satstype,
                sats = curr.sats,
                antall = prev.nonNullAntallOrZero + curr.nonNullAntallOrZero,
                belop = prev.nonNullBelopOrZero + curr.nonNullBelopOrZero
            )
        }
    private val Underytelse.nonNullAntallOrZero get() = this.antall ?: 0.0
    private val Underytelse.nonNullBelopOrZero get() = this.belop ?: 0.0
}
