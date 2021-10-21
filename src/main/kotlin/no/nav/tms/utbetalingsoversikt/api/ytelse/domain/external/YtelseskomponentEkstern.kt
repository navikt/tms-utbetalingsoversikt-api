package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class YtelseskomponentEkstern(
    val ytelseskomponenttype: String?,
    @Serializable(with = BigDecimalSerializer::class) val satsbeloep: BigDecimal?,
    val satstype: String?,
    val satsantall: Int?,
    @Serializable(with = BigDecimalSerializer::class) val ytelseskomponentbeloep: BigDecimal?,
)
