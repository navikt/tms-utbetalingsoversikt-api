package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class SkattEsktern(
    @Serializable(with = BigDecimalSerializer::class) val skattebeloep: BigDecimal?
)
