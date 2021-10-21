package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable

@Serializable
data class Utbetalingsoppslag(
    val ident: String,
    val rolle: RolleEkstern,
    val periode: PeriodeEkstern,
    val periodetype: PeriodetypeEkstern,
)

@Serializable
enum class PeriodetypeEkstern(val databaseverdi: String) {
    UTBETALINGSPERIODE("Utbetalingsperiode"),
    YTELSESPERIODE("Ytelsesperiode")
}

@Serializable
enum class RolleEkstern(val databaseverdi: String) {
    UTBETALT_TIL("UtbetaltTil"),
    RETTIGHETSHAVER("Rettighetshaver")
}
