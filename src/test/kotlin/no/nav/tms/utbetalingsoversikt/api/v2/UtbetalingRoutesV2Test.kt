package no.nav.tms.utbetalingsoversikt.api.v2


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.util.pipeline.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tms.token.support.idporten.sidecar.mock.LevelOfAssurance.HIGH
import no.nav.tms.token.support.idporten.sidecar.mock.installIdPortenAuthMock
import no.nav.tms.token.support.tokendings.exchange.TokendingsService
import no.nav.tms.utbetalingsoversikt.api.config.jsonConfig
import no.nav.tms.utbetalingsoversikt.api.config.utbetalingApi
import no.nav.tms.utbetalingsoversikt.api.ytelse.SokosUtbetalingConsumer
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
    fun `oppsumerer alle ytelser i periode`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = URL(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )

        withExternalServiceResponse(
            5.tidligereYtelser(
                expectedKontantstøtte = 2600.12,
                expectedForeldrepenger = 79467.0,
                expectedØkonomiskSosialhjelp = 10365.0,
                expectedTrekk = 7659.0,
                expectedUtbetalt = 92432.0 - 7659.0
            )
        ) { true }


        client.get("/utbetalinger/alle").assert {
            status shouldBe HttpStatusCode.OK
            val responseBody = objectMapper.readTree(bodyAsText())
            responseBody["utbetalingerIPeriode"]["ytelser"].toList().apply {
                withClue("Foreldrepenger") {
                    find { it["ytelse"].asText() == "Foreldrepenger" }!!["beløp"].asDouble() shouldBe (79467.0 * 5)
                }
                withClue("Økonomisk Sosialhjelp") {
                    find { it["ytelse"].asText() == "Økonomisk Sosialhjelp" }!!["beløp"].asDouble() shouldBe (10365.0 * 5)

                }
                withClue("Kontantstøtte") {
                    find { it["ytelse"].asText() == "Kontantstøtte" }!!["beløp"].asDouble() shouldBe (2600.12.toBigDecimal() * 5.0.toBigDecimal()).toDouble()
                }
            }
            val expectedTotalBrutto = (79467.0 + 10365.0 + 2600.12) * 5
            withClue("trekk") {
                responseBody["utbetalingerIPeriode"]["trekk"].asDouble() shouldBe 7659.0
            }
            withClue("brutto") {
                responseBody["utbetalingerIPeriode"]["brutto"].asDouble() shouldBe expectedTotalBrutto
            }

            withClue("netto") {
                responseBody["utbetalingerIPeriode"]["netto"].asDouble() shouldBe expectedTotalBrutto - 7659.0
            }
        }
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
          ${nesteYtelseJson(15)},   
          ${nesteYtelseJson(1)},     
          ${nesteYtelseJson(20)}    
           ]
            
        """.trimIndent()
        ) { true }


        client.get("/utbetalinger/alle").assert {
            status shouldBe HttpStatusCode.OK
            val responseBody = objectMapper.readTree(bodyAsText())

            val tidligere = responseBody["tidligere"].toList()
            tidligere.size shouldBe 4
            tidligere.shouldBeInDescedingYearMonthOrder()
            tidligere.find { it["år"].asInt() == 2023 && it["måned"].asInt() == 8 }.apply {
                require(this != null)
                this["utbetalinger"].toList().apply {
                    shouldBeInDescendingDateOrder()
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
                shouldBeInAscendingDateOrder()
            }
        }
    }

    @Test
    fun `henter utbetalinger for definert fom og tom`() = testApplication {
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
            body = """
          ${tidligereUtbetalingerJson.substring(0, tidligereUtbetalingerJson.lastIndexOf("]"))},
          ${nesteYtelseJson(1)},    
          ${nesteYtelseJson(15)},    
          ${nesteYtelseJson(20)}    
           ]
            
        """.trimIndent()
        ) {
            val fomtom = objectMapper.readTree(call.receiveText())
            fomtom["periode"]["fom"].asText() == "2023-05-29" && fomtom["periode"]["tom"].asText() == "2023-08-30"
        }


        client.get("/utbetalinger/alle?fom=20230529&tom=20230829").assert {
            status shouldBe HttpStatusCode.OK
            val responseBody = objectMapper.readTree(bodyAsText())

            val tidligere = responseBody["tidligere"].toList()
            tidligere.size shouldBe 4
            tidligere.shouldBeInDescedingYearMonthOrder()

            responseBody["neste"].toList().apply {
                size shouldBe 9
                shouldBeInAscendingDateOrder()
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
        withExternalServiceResponse(responseJsonText) { true == true }
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

        withExternalServiceResponse("[]") { true == true }
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


    @Test
    fun `returnerer detaljertinfo om utbeatling for en spesifikk utbetaling av en ytelse`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = URL(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )

        //fake et eller annet for YtelseUtil her
        client.get("/utbetalinger/ydaj31").assert {
            status shouldBe HttpStatusCode.OK
            val json = jacksonObjectMapper().readTree(bodyAsText())
            json["ytelse"].asText() shouldBe "Arbeidsavklaringspenger"
            json["erUtbetalt"].asText() shouldBe true
            json["ytelsePeriode"].apply {
                this["fom"] shouldBe "2023.08.01" //sjekk format
                this["fom"] shouldBe "2023.08.14" //sjekk format
            }
            json["ytelseDato"].asText() shouldBe "2023.08.15"
            json["kontonummer"].asText() shouldBe "xxxxxx9876"
            json["underytelse"].toList().apply {
                size shouldBe 2
                this[0].apply {
                    this["beskrivelse"].asText() shouldBe "Grunnbeløp"
                    this["sats"].asDouble() shouldBe 500.25
                    this["antall"].asInt() shouldBe 3
                    this["beløp"].asDouble() shouldBe 1500.75 //sats*antall
                }
                json[1].apply {
                    this["beskrivelse"].asText() shouldBe "Annet beløp"
                    this["sats"].asDouble() shouldBe 300.25
                    this["antall"].asInt() shouldBe 4
                    this["beløp"].asDouble() shouldBe 1201.00 //sats*antall
                }
            }
            json["trekk"].toList().apply {
                this[0].apply {
                    this["type"].asText() shouldBe "Skatt"
                    this["beløp"].asDouble() shouldBe 238.74
                }
                this[0].apply {
                    this["type"].asText() shouldBe "Annet trekk"
                    this["beløp"].asDouble() shouldBe 203.66
                }
            }

            json["melding"].asText() shouldBe "En eller annen melding"
            json["bruttoUtbetalt"].asDouble() shouldBe 2701.75
            json["nettoUtbetalt"].asDouble() shouldBe 2259.35
        }
    }

    private fun ApplicationTestBuilder.withExternalServiceResponse(
        body: String,
        replyIf: suspend PipelineContext<Unit, ApplicationCall>.() -> Boolean = { true }
    ) {
        externalServices {
            hosts(testHost) {
                install(ContentNegotiation) {
                    json(jsonConfig())
                }
                routing {
                    route("hent-utbetalingsinformasjon/ekstern") {
                        post {
                            if (replyIf())
                                call.respondText(contentType = ContentType.Application.Json, text = body)
                            else
                                call.respondText(contentType = ContentType.Application.Json, text = "[]")
                        }
                    }

                }

            }
        }
    }
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
