package no.nav.tms.utbetalingsoversikt.api.v2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import org.amshove.kluent.shouldBeOnOrAfter
import org.amshove.kluent.shouldBeOnOrBefore
import org.intellij.lang.annotations.Language
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


    @Test
    fun `henter alle utbetalinger i kronologisk rekkefølge`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = URL(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )

        val tidligereUtbetalingerJson = File("src/test/resources/alle_utbetalinger_tidligere.json").readText()

        withExternalServiceResponse(
            """
          ${tidligereUtbetalingerJson.substring(0, tidligereUtbetalingerJson.lastIndexOf("]"))},
          ${nesteYtelseJson(1)},    
          ${nesteYtelseJson(15)},    
          ${nesteYtelseJson(20)}    
           ]
            
        """.trimIndent()
        )


        client.get("/utbetalinger/alle").assert {
            status shouldBe HttpStatusCode.OK
            val responseBody = objectMapper.readTree(bodyAsText())

            val tidligere = responseBody["tidligere"].toList()
            tidligere.size shouldBe 4
            tidligere.shouldBeInDescedingYearMonthOrder()
            tidligere.find { it["år"].asInt() == 2023 && it["måned"].asInt() == 8 }.apply {
                require(this != null)
                this["utbetalinger"].toList().apply {
                    shouldBeInDescedingDateOrder()
                    size shouldBe 5
                    count { it["ytelse"].asText() == "Foreldrepenger" } shouldBe 2
                    count { it["ytelse"].asText() == "Økonomisk sosialhjelp" } shouldBe 2
                    count { it["ytelse"].asText() == "Dagpenger" } shouldBe 1
                    count { it["dato"].asText() == "2023-08-24" } shouldBe 3
                    count { it["dato"].asText() == "2023-08-02" } shouldBe 2

                }
            }

            tidligere.find { it["år"].asInt() == 2023 && it["måned"].asInt() == 2 }.apply {
                require(this != null)
                this["utbetalinger"].toList().apply {
                    size shouldBe 2
                    count { it["ytelse"].asText() == "Foreldrepenger" } shouldBe 1
                    count { it["ytelse"].asText() == "Økonomisk sosialhjelp" } shouldBe 1
                }
            }

            tidligere.find { it["år"].asInt() == 2022 && it["måned"].asInt() == 8 }!!["utbetalinger"].toList().size shouldBe 2
            tidligere.find { it["år"].asInt() == 2022 && it["måned"].asInt() == 1 }!!["utbetalinger"].toList().size shouldBe 3

            responseBody["neste"].toList().apply {
                size shouldBe 9
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

private fun List<JsonNode>.shouldBeInDescedingYearMonthOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    map { LocalDate.of(it["år"].asInt(), it["måned"].asInt(), 1) }.forEach { utbetalingsdato ->
        utbetalingsdato shouldBeBefore sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}
private fun List<JsonNode>.shouldBeInDescedingDateOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    map { LocalDate.parse(it["dato"].asText()) }.forEach { utbetalingsdato ->
        utbetalingsdato shouldBeOnOrBefore  sisteUtbetalinsgDato
        sisteUtbetalinsgDato = utbetalingsdato
    }
}


private fun List<JsonNode>.shouldBeInAscendingOrder() {
    var sisteUtbetalinsgDato = LocalDate.now()
    map { LocalDate.parse(it["dato"].asText()) }.forEach { utbetalingsdato ->
        utbetalingsdato shouldBeOnOrAfter  sisteUtbetalinsgDato
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


@Language("JSON")
private fun nesteYtelseJson(plusDays: Long = 5) = """
      {
        "posteringsdato": "${LocalDate.now().plusDays(plusDays)}",
        "utbetaltTil": {
          "aktoertype": "PERSON",
          "ident": "123345567",
          "navn": "string"
        },
        "utbetalingNettobeloep": 8700,
        "utbetalingsmelding": "string",
        "utbetalingsdato": null,
        "forfallsdato": "${LocalDate.now().plusDays(plusDays)}",
        "utbetaltTilKonto": {
          "kontonummer": "888777666555444",
          "kontotype": "Norsk bank"
        },
        "utbetalingsmetode": "Til konto",
        "utbetalingsstatus": "something",
        "ytelseListe": [
          {
            "ytelsestype": "Dagpenger",
            "ytelsesperiode": {
              "fom": "${LocalDate.now().plusDays(plusDays)}",
              "tom": "${LocalDate.now().plusDays(plusDays)}"
            },
            "ytelseskomponentListe": [
              {
                "ytelseskomponenttype": "string",
                "satsbeloep": 999,
                "satstype": "string",
                "satsantall": 2.5,
                "ytelseskomponentbeloep": 42
              }
            ],
            "ytelseskomponentersum": 111.22,
            "trekkListe": [
              {
                "trekktype": "string",
                "trekkbeloep": 100,
                "kreditor": "string"
              }
            ],
            "trekksum": 1000,
            "skattListe": [
              {
                "skattebeloep": 99.9
              }
            ],
            "skattsum": 1000.5,
            "ytelseNettobeloep": 3788,
            "bilagsnummer": "84172491",
            "rettighetshaver": {
              "aktoertype": "PERSON",
              "ident": "1234567890g",
              "navn": "Navn Navnesen"
            }
          },
          {
            "ytelsestype": "Foreldrepenger",
            "ytelsesperiode": {
              "fom": "${LocalDate.now().plusDays(plusDays)}",
              "tom": "${LocalDate.now().plusDays(plusDays)}"
            },
            "ytelseskomponentListe": [
              {
                "ytelseskomponenttype": "string",
                "satsbeloep": 999,
                "satstype": "string",
                "satsantall": 2.5,
                "ytelseskomponentbeloep": 42
              }
            ],
            "ytelseskomponentersum": 111.22,
            "trekkListe": [
              {
                "trekktype": "string",
                "trekkbeloep": 100,
                "kreditor": "string"
              }
            ],
            "trekksum": 1000,
            "skattListe": [
              {
                "skattebeloep": 99.9
              }
            ],
            "skattsum": 1000.5,
            "ytelseNettobeloep": 2600.87,
            "bilagsnummer": "84172491",
            "rettighetshaver": {
              "aktoertype": "PERSON",
              "ident": "1234567890g",
              "navn": "Navn Navnesen"
            }
          },
          {
            "ytelsestype": "Økonomisk sosialhjelp",
            "ytelsesperiode": {
              "fom": "${LocalDate.now().plusDays(plusDays)}",
              "tom": "${LocalDate.now().plusDays(plusDays)}"
            },
            "ytelseskomponentListe": [
              {
                "ytelseskomponenttype": "string",
                "satsbeloep": 999,
                "satstype": "string",
                "satsantall": 2.5,
                "ytelseskomponentbeloep": 42
              }
            ],
            "ytelseskomponentersum": 111.22,
            "trekkListe": [
              {
                "trekktype": "string",
                "trekkbeloep": 100,
                "kreditor": "string"
              }
            ],
            "trekksum": 1000,
            "skattListe": [
              {
                "skattebeloep": 99.9
              }
            ],
            "skattsum": 1000.5,
            "ytelseNettobeloep": 2311.13,
            "bilagsnummer": "84172491",
            "rettighetshaver": {
              "aktoertype": "PERSON",
              "ident": "12345678909",
              "navn": "Navn Navnesen"
            }
          }
        ]
      }
""".trimIndent()