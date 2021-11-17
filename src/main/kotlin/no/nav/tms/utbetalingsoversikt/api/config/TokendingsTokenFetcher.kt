package no.nav.tms.utbetalingsoversikt.api.config

import no.nav.tms.token.support.tokendings.exchange.TokendingsService

class TokendingsTokenFetcher(private val tokendingsService: TokendingsService, private val sokosUtbetaldataClientId: String) {

    suspend fun getSokosUtbetaldataToken(userToken: String): String {
        return tokendingsService.exchangeToken(userToken, sokosUtbetaldataClientId)
    }
}
