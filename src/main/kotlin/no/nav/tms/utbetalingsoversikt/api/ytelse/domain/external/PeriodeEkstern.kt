package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class PeriodeEkstern(
    @Serializable(with = LocalDateSerializer::class) val fom: LocalDate,
    @Serializable(with = LocalDateSerializer::class) val tom: LocalDate,
)
