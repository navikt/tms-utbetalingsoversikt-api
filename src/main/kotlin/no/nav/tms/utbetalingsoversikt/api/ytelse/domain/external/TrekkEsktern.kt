package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class TrekkEsktern(
    val trekktype: String? = null,
    @Serializable(with = BigDecimalSerializer::class) val trekkbeloep: BigDecimal? = null,
    val kreditor: String? = null,
)
