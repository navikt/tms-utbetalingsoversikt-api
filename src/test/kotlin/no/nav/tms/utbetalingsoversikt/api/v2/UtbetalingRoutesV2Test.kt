package no.nav.tms.utbetalingsoversikt.api.v2

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.mockk
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance
import no.nav.tms.token.support.idporten.sidecar.mock.LevelOfAssurance.HIGH
import no.nav.tms.token.support.idporten.sidecar.mock.installIdPortenAuthMock
import no.nav.tms.utbetalingsoversikt.api.config.jsonConfig
import no.nav.tms.utbetalingsoversikt.api.config.utbetalingApi
import org.junit.jupiter.api.Test


class UtbetalingRoutesV2Test {

    @Test
    fun `henter alle utbetalinger i kronologisk rekkefølge`() = testApplication {
        testApi()
        client.get("/utbetalinger/alle").assert {
            status shouldBe HttpStatusCode.OK
        }
    }

    @Test
    fun `henter siste utbetalinger`() = testApplication {
        testApi()
        client.get("/utbetalinger/siste").assert {
            status shouldBe HttpStatusCode.OK
        }
    }

}

private fun HttpResponse.assert(function: HttpResponse.() -> Unit) {
    function()
}


private fun ApplicationTestBuilder.testApi() {
    val httpClient = createClient { jsonConfig() }
    application {
        utbetalingApi(
            httpClient = httpClient,
            utbetalingService = mockk(),
            authConfig = {
                installIdPortenAuthMock {
                    setAsDefault = true
                    staticLevelOfAssurance = HIGH
                    staticUserPid="12345"
                    alwaysAuthenticated = true
                }
            },
            corsAllowedSchemes = listOf("https", "http"),
            corsAllowedOrigins = "*",
        )
    }
}