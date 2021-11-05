package no.nav.tms.utbetalingsoversikt.api.config

import no.nav.personbruker.dittnav.common.util.config.BooleanEnvVar
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVarAsList
import no.nav.personbruker.dittnav.common.util.config.UrlEnvVar.getEnvVarAsURL
import java.net.URL

data class Environment(
    val corsAllowedOrigins: String = getEnvVar("CORS_ALLOWED_ORIGINS"),
    val corsAllowedSchemes: List<String> = getEnvVarAsList("CORS_ALLOWED_SCHEMES"),
    val postLogoutUrl: String = getEnvVar("POST_LOGOUT_URL"),
    val sokosUtebatlingAzureClientId: String = getEnvVar("SOKOS_UTBETALING_AZURE_CLIENT_ID"),
    val sokosUtebatlingTokenxClientId: String = getEnvVar("SOKOS_UTBETALING_TOKENX_CLIENT_ID"),
    val sokosUtbetalingUrl: URL = getEnvVarAsURL("SOKOS_UTBETALDATA_URL"),
    val enableDebugApi: Boolean = BooleanEnvVar.getEnvVarAsBoolean("ENABLE_DEBUG_API", default = false)
)

