package no.nav.tms.utbetalingsoversikt.api.config

import no.nav.personbruker.dittnav.common.util.config.IntEnvVar.getEnvVarAsInt
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVarAsList
import no.nav.personbruker.dittnav.common.util.config.UrlEnvVar.getEnvVarAsURL
import java.net.URL

data class Environment(
    val rootPath: String = "tms-utbetalingsoversikt-api",
    val corsAllowedOrigins: String = getEnvVar("CORS_ALLOWED_ORIGINS"),
    val port: Int = getEnvVarAsInt("PORT", default = 8080),
    val corsAllowedSchemes: List<String> = getEnvVarAsList("CORS_ALLOWED_SCHEMES"),
    val sokosUtbetalingClientId: String = getEnvVar("SOKOS_UTBETALING_TOKENX_CLIENT_ID"),
    val sokosUtbetalingUrl: URL = getEnvVarAsURL("SOKOS_UTBETALDATA_URL")
)

