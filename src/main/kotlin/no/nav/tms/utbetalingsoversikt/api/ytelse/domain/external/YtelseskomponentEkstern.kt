package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable
import no.nav.personbruker.dittnav.common.logging.util.logger

@Serializable
data class YtelseskomponentEkstern(
    val ytelseskomponenttype: String? = null,
    val satsbeloep: Double? = null,
    val satstype: String? = null,
    val satsantall: Double? = null,
    val ytelseskomponentbeloep: Double? = null,
) {
    init {
        logger.info("Beløp: $satsbeloep, antall: $satsantall")
    }
}
