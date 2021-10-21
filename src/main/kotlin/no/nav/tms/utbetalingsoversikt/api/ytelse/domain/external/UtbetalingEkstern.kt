package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.BigDecimalSerializer
import no.nav.tms.utbetalingsoversikt.api.config.LocalDateSerializer
import java.math.BigDecimal
import java.time.LocalDate

@Serializable
data class UtbetalingEkstern(
    val utbetaltTil: AktoerEkstern,
    val utbetalingsmetode: String,
    val utbetalingsstatus: String,
    @Serializable(with = LocalDateSerializer::class) val posteringsdato: LocalDate,
    @Serializable(with = LocalDateSerializer::class) val forfallsdato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class) val utbetalingsdato: LocalDate?,
    @Serializable(with = BigDecimalSerializer::class) val utbetalingNettobeloep: BigDecimal?,
    val utbetalingsmelding: String?,
    val utbetaltTilKonto: BankkontoEsktern?,
    val ytelseListe: List<YtelseEkstern> = emptyList(),
)















