package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class YtelseskomponentEkstern(
    val ytelseskomponenttype: String? = null,
    @Serializable(with = BigDecimalSerializer::class) val satsbeloep: BigDecimal? = null,
    val satstype: String? = null,
    val satsantall: Int? = null,
    @Serializable(with = BigDecimalSerializer::class) val ytelseskomponentbeloep: BigDecimal? = null,
)
