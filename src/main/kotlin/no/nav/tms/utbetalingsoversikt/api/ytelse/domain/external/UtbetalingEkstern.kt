package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import java.math.BigDecimal
import java.time.LocalDate

data class UtbetalingEkstern(
    val utbetaltTil: AktoerEkstern,
    val utbetalingsmetode: String,
    val utbetalingsstatus: String,
    val posteringsdato: LocalDate,
    val forfallsdato: LocalDate?,
    val utbetalingsdato: LocalDate?,
    val utbetalingNettobeloep: BigDecimal?,
    val utbetalingsmelding: String?,
    val utbetaltTilKonto: BankkontoEsktern?,
    val ytelseListe: List<YtelseEkstern> = emptyList(),
)















