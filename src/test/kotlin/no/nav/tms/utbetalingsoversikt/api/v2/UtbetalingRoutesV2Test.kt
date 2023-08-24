package no.nav.tms.utbetalingsoversikt.api.v2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tms.token.support.idporten.sidecar.mock.LevelOfAssurance.HIGH
import no.nav.tms.token.support.idporten.sidecar.mock.installIdPortenAuthMock
import no.nav.tms.token.support.tokendings.exchange.TokendingsService
import no.nav.tms.utbetalingsoversikt.api.config.jsonConfig
import no.nav.tms.utbetalingsoversikt.api.config.utbetalingApi
import no.nav.tms.utbetalingsoversikt.api.ytelse.SokosUtbetalingConsumer
import org.amshove.kluent.shouldBeAfter
import org.amshove.kluent.shouldBeBefore
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URL
import java.time.LocalDate


class UtbetalingRoutesV2Test {
    private val objectMapper = jacksonObjectMapper()
    private val testHost = "https://utbetaling.ekstern.test"
    private val tokendingsMockk = mockk<TokendingsService>().also {
        coEvery {
            it.exchangeToken(any(), any())
        } returns "<dummytoken>"
    }

    @Disabled
    @Test
    fun `henter alle utbetalinger i kronologisk rekkefølge`() = testApplication {
        testApi()
        client.get("/utbetalinger/alle").assert {
            status shouldBe HttpStatusCode.OK
            val responseBody = objectMapper.readTree(bodyAsText())
            responseBody["neste"].toList().size shouldBe 2
            val tidligere = responseBody["tidligere"].toList()
            tidligere.shouldBeInDescedingOrder()
            tidligere.find { it["år"].asInt() == 2023 && it["måned"].asInt() == 8 }.apply {
                require(this != null)
                this["ytelser"].toList().apply {
                    this.count { it["ytelse"].asText() == "AAP" } shouldBe 2
                    this.count { it["ytelse"].asText() == "DAG" } shouldBe 1
                }
            }

            tidligere.find { it["år"].asInt() == 2023 && it["måned"].asInt() == 8 }.apply {
                require(this != null)
                this["ytelser"].toList().apply {
                    this.count { it["ytelse"].asText() == "AAP" } shouldBe 2
                    this.count { it["ytelse"].asText() == "DAG" } shouldBe 1
                }
            }

            tidligere.find { it["år"].asInt() == 2023 && it["måned"].asInt() == 2 }.apply {
                require(this != null)
                this["ytelser"].toList().apply {
                    this.count { it["ytelse"].asText() == "ANNNEN" } shouldBe 2
                }
            }

            tidligere.find { it["år"].asInt() == 2022 && it["måned"].asInt() == 8 }.apply {
                require(this != null)
                this["ytelser"].toList().apply {
                    this.count { it["ytelse"].asText() == "ANNNEN" } shouldBe 2
                }
            }

            responseBody["neste"].toList().apply {
                size shouldBe 3
                shouldBeInAscendingOrder()
            }
        }
    }

    @Test
    fun `henter siste utbetalinger`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = URL(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )
        val responseJsonText = File("src/test/resources/siste_utbetaling_test.json").readText()
        withExternalServiceResponse(responseJsonText)
        client.get("/utbetalinger/siste").assert {
            status shouldBe HttpStatusCode.OK
            objectMapper.readTree(bodyAsText()).apply {
                this["harUtbetaling"].asBoolean() shouldBe true
                this["sisteUtbetaling"].asInt() shouldBe 8700
                this["ytelser"].let { objectMapper.convertValue(it, Map::class.java) }.apply {
                    this["Dagpenger"] shouldBe 3788
                    this["Foreldrepenger"] shouldBe 2600.87
                    this["Økonomisk sosialhjelp"] shouldBe 2311.13

                }
            }
        }
    }

    @Test
    fun `returner tomt objekt hvis det ikke finnes noen utbetalinger de siste 3 månedene`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = URL(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )

        withExternalServiceResponse("[]")
        client.get("/utbetalinger/siste").assert {
            status shouldBe HttpStatusCode.OK
            objectMapper.readTree(bodyAsText()).apply {
                this["harUtbetaling"].asBoolean() shouldBe false
                this["sisteUtbetaling"].asDouble() shouldBe 0.0
                this["ytelser"].toList() shouldBe emptyList()
                this["dato"].isNull shouldBe true
            }
        }
    }

    private fun ApplicationTestBuilder.withExternalServiceResponse(body: String) {
        externalServices {
            hosts(testHost) {
                install(ContentNegotiation) {
                    json(jsonConfig())
                }
                routing {
                    route("hent-utbetalingsinformasjon/ekstern") {
                        post {
                            call.respondText(contentType = ContentType.Application.Json, text = body)
                        }
                    }

                }

            }
        }
    }
}

private fun List<JsonNode>.shouldBeInDescedingOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    map { LocalDate.of(it["år"].asInt(), it["måned"].asInt(), 1) }.forEach { utbetalingsdato ->
        utbetalingsdato shouldBeBefore sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}

private fun List<JsonNode>.shouldBeInAscendingOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    map { LocalDate.of(it["år"].asInt(), it["måned"].asInt(), 1) }.forEach { utbetalingsdato ->
        utbetalingsdato shouldBeAfter sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}

private suspend fun HttpResponse.assert(function: suspend HttpResponse.() -> Unit) {
    function()
}


private fun ApplicationTestBuilder.testApi(sokosUtbetalingConsumer: SokosUtbetalingConsumer = mockk()) {
    val httpClient = createClient { jsonConfig() }
    application {
        utbetalingApi(
            httpClient = httpClient,
            sokosUtbetalingConsumer = sokosUtbetalingConsumer,
            authConfig = {
                installIdPortenAuthMock {
                    setAsDefault = true
                    staticLevelOfAssurance = HIGH
                    staticUserPid = "12345"
                    alwaysAuthenticated = true
                }
            },
            corsAllowedSchemes = listOf("https", "http"),
            corsAllowedOrigins = "*",
        )
    }
}

private val ApplicationTestBuilder.sokosHttpClient
    get() = createClient {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(jsonConfig())
        }
        install(HttpTimeout)
    }