package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.config.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class YtelseEkstern(
    val ytelsestype: String? = null,
    val ytelsesperiode: PeriodeEkstern,
    @Serializable(with = BigDecimalSerializer::class) val ytelseNettobeloep: BigDecimal,
    val rettighetshaver: AktoerEkstern,
    @Serializable(with = BigDecimalSerializer::class) val skattsum: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) val trekksum: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) val ytelseskomponentersum: BigDecimal,

    val skattListe: List<SkattEsktern>? = null,
    val trekkListe: List<TrekkEsktern>? = null,
    val ytelseskomponentListe: List<YtelseskomponentEkstern>? = null,

    val bilagsnummer: String? = null,
    val refundertForOrg: AktoerEkstern? = null,
)
