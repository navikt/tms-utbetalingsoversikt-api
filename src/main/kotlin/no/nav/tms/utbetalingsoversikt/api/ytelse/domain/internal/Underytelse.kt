package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal

import kotlinx.serialization.Serializable

@Serializable
data class Underytelse (
    val beskrivelse:  String?,
    val satstype:  String?,
    val sats:  Double?,
    val antall:  Int?,
    val belop:  Double?,
)
