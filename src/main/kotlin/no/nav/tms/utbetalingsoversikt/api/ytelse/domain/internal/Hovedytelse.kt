package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class Hovedytelse(
    val ytelse: String,
    val status: String,
    @SerialName("ytelse_dato") @Serializable(with = LocalDateSerializer::class) val ytelseDato: LocalDate?,
    @SerialName("forfall_dato") @Serializable(with = LocalDateSerializer::class) val forfallDato: LocalDate?,
    @SerialName("ytelse_periode") val ytelsePeriode: Periode,
    @SerialName("utbetalt_til") val utbetaltTil: String?,
    val kontonummer: String,
    val underytelser: List<Underytelse>,
    val trekk: List<Trekk>,
    @SerialName("er_utbetalt") val erUtbetalt: Boolean,
    val rettighetshaver: Rettighetshaver,
    val melding: String
)
