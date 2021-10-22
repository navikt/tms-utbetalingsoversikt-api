package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class Hovedytelse(
    val id: Int,
    val ytelse: String,
    val status: String,
    @Serializable(with = LocalDateSerializer::class) val ytelseDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class) val forfallDato: LocalDate?,
    val ytelsePeriode: Periode?,
    val utbetaltTil: String?,
    val kontonummer: String,
    val underytelser: List<Underytelse>,
    val trekk: List<Trekk>,
    val erUtbetalt: Boolean,
    val rettighetshaver: Rettighetshaver,
    val melding: String
)
