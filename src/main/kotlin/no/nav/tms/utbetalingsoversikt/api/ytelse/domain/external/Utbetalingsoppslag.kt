package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

data class Utbetalingsoppslag(
    val ident: String,
    val rolle: RolleEkstern,
    val periode: PeriodeEkstern,
    val periodetype: PeriodetypeEkstern,
)

enum class PeriodetypeEkstern(val databaseverdi: String) {
    UTBETALINGSPERIODE("Utbetalingsperiode"),
    YTELSESPERIODE("Ytelsesperiode")
}

enum class RolleEkstern(val databaseverdi: String) {
    UTBETALT_TIL("UtbetaltTil"),
    RETTIGHETSHAVER("Rettighetshaver")
}
