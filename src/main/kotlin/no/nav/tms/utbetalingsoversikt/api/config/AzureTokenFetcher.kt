package no.nav.tms.utbetalingsoversikt.api.config

import no.nav.tms.token.support.azure.exchange.AzureService

class AzureTokenFetcher(private val azureService: AzureService, private val sokosUtbetaldataClientId: String) {

    suspend fun getSokosUtbetaldataToken(): String {
        return azureService.getAccessToken(sokosUtbetaldataClientId)
    }
}
