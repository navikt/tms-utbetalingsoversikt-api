package no.nav.tms.utbetalingsoversikt.api.config

import no.nav.personbruker.dittnav.common.util.config.IntEnvVar.getEnvVarAsInt
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVarAsList
import no.nav.personbruker.dittnav.common.util.config.UrlEnvVar.getEnvVarAsURL
import java.net.URL

data class Environment(
    val sokosUtbetalingClientId: String = getEnvVar("SOKOS_UTBETALING_TOKENX_CLIENT_ID"),
    val sokosUtbetalingUrl: URL = getEnvVarAsURL("SOKOS_UTBETALDATA_URL")
)

