package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable

@Serializable
data class YtelseEkstern(
    val ytelsestype: String? = null,
    val ytelsesperiode: PeriodeEkstern,
    val ytelseNettobeloep: Double,
    val rettighetshaver: AktoerEkstern,
    val skattsum: Double,
    val trekksum: Double,
    val ytelseskomponentersum: Double,

    val skattListe: List<SkattEkstern>? = null,
    val trekkListe: List<TrekkEkstern>? = null,
    val ytelseskomponentListe: List<YtelseskomponentEkstern>? = null,

    val bilagsnummer: String? = null,
    val refundertForOrg: AktoerEkstern? = null,
)
