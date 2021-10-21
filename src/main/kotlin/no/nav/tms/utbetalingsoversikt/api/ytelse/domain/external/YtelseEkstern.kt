package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import java.math.BigDecimal

data class YtelseEkstern(
    val ytelsestype: String?,
    val ytelsesperiode: PeriodeEkstern,
    val ytelseNettobeloep: BigDecimal,
    val rettighetshaver: AktoerEkstern,
    val skattsum: BigDecimal,
    val trekksum: BigDecimal,
    val ytelseskomponentersum: BigDecimal,

    val skattListe: List<SkattEsktern>? = null,
    val trekkListe: List<TrekkEsktern>? = null,
    val ytelseskomponentListe: List<YtelseskomponentEkstern>? = null,

    val bilagsnummer: String?,
    val refundertForOrg: AktoerEkstern?,
)
