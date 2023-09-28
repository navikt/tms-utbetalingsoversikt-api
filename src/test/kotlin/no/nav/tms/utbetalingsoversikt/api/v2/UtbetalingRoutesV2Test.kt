package no.nav.tms.utbetalingsoversikt.api.v2


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import no.nav.tms.token.support.idporten.sidecar.mock.LevelOfAssurance.HIGH
import no.nav.tms.token.support.idporten.sidecar.mock.installIdPortenAuthMock
import no.nav.tms.token.support.tokendings.exchange.TokendingsService
import no.nav.tms.utbetalingsoversikt.api.config.jsonConfig
import no.nav.tms.utbetalingsoversikt.api.config.utbetalingApi
import no.nav.tms.utbetalingsoversikt.api.utbetaling.YtelseIdUtil
import no.nav.tms.utbetalingsoversikt.api.ytelse.SokosUtbetalingConsumer
import org.junit.jupiter.api.BeforeAll
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
    private val spesifikkUtbetalingRespons = File("src/test/resources/utbetaling_detalj_test.json").readText()
    private val utbetaltTilRespons = File("src/test/resources/utbetaling_detalj_utbetalttil_test.json").readText()

    /*
    @Disabled
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
    }*/

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

        val tidligereUtbetalingerJson = File("src/test/resources/alle_utbetalinger_tidligere_default.json").readText()

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
            tidligere.shouldBeInDescedingYearMonthOrder()
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
            fomtom["periode"]["fom"].asText() == "2023-05-09" && fomtom["periode"]["tom"].asText() == "2023-08-30"
        }


        client.get("/utbetalinger/alle?fom=20230529&tom=20230829").assert {
            status shouldBe HttpStatusCode.OK
            val responseBody = objectMapper.readTree(bodyAsText())

            val tidligere = responseBody["tidligere"].toList()
            tidligere.size shouldBe 3
            tidligere.shouldBeInDescedingYearMonthOrder()
            tidligere.find { it["år"].asInt() == 2023 && it["måned"].asInt() == 8 }.apply {
                require(this != null)
                this["utbetalinger"].toList().apply {
                    shouldBeInDescendingDateOrder()
                    size shouldBe 7
                    count { it["ytelse"].asText() == "Foreldrepenger" } shouldBe 3
                    count { it["ytelse"].asText() == "Økonomisk sosialhjelp" } shouldBe 3
                    count { it["ytelse"].asText() == "Dagpenger" } shouldBe 1
                    count { it["dato"].asText() == "2023-08-24" } shouldBe 3
                    count { it["dato"].asText() == "2023-08-02" } shouldBe 4

                }
            }

            tidligere.find { it["år"].asInt() == 2023 && it["måned"].asInt() == 5 } shouldNotBe null
            tidligere.find { it["år"].asInt() == 2023 && it["måned"].asInt() == 7 } shouldNotBe null

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
        withExternalServiceResponse(responseJsonText) { true }
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

        withExternalServiceResponse("[]") { true }
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
    fun `returnerer detaljertinfo om utbetaling for spesifikk utbetaling av en ytelse`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = URL(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )
        withExternalServiceResponse(spesifikkUtbetalingRespons) { true }
        mockkObject(YtelseIdUtil)
        every { YtelseIdUtil.unmarshalDateFromId("ydaj31") } returns LocalDate.now()
        every { YtelseIdUtil.calculateId("2023-08-24", any()) } returns "ydaj31"

        client.get("/utbetalinger/ydaj31").assert {
            status shouldBe HttpStatusCode.OK
            val json = jacksonObjectMapper().readTree(bodyAsText())
            json["ytelse"].asText() shouldBe "Arbeidsavklaringspenger"
            json["erUtbetalt"].asBoolean() shouldBe true
            json["ytelsePeriode"].apply {
                this["fom"].asText() shouldBe "2023-08-01"
                this["tom"].asText() shouldBe "2023-08-14"
            }
            json["ytelseDato"].asText() shouldBe "2023-08-15"
            json["kontonummer"].asText() shouldBe "xxxxxx39876"
            json["utbetaltTil"].asText() shouldBe "xxxxxx39876"
            json["underytelse"].toList().apply {
                size shouldBe 2
                this[0].apply {
                    this["beskrivelse"].asText() shouldBe "Grunnbeløp"
                    this["satstype"].asText() shouldBe "et eller annet"
                    this["sats"].asDouble() shouldBe 500.25
                    this["antall"].asDouble() shouldBe 3
                    this["beløp"].asDouble() shouldBe 1500.75
                }
                this[1].apply {
                    this["beskrivelse"].asText() shouldBe "Annet beløp"
                    withClue("satstype skal være null") { this["satstype"].isNull shouldBe true }
                    this["sats"].asDouble() shouldBe 0.0
                    this["antall"].asDouble() shouldBe 0.0
                    this["beløp"].asDouble() shouldBe 400
                }
            }
            json["trekk"].toList().apply {
                size shouldBe 3
                this.find { it["type"].asText() == "Skatt" }.let { trekk ->
                    require(trekk != null)
                    trekk["beløp"].asDouble() shouldBe -300.25
                }
                this.find { it["type"].asText() == "Annet trekk" }.let { trekk ->
                    require(trekk != null)
                    trekk["beløp"].asDouble() shouldBe -600.50
                }
                this.find { it["type"].asText() == "Skattetrekk" }.let { trekk ->
                    require(trekk != null)
                    trekk["beløp"].asDouble() shouldBe -99.9
                }
            }

            json["melding"].asText() shouldBe "En eller annen melding"
            json["bruttoUtbetalt"].asDouble() shouldBe 1900.75
            json["nettoUtbetalt"].asDouble() shouldBe 900.1
        }
    }

    @Test
    fun `bad request for ytelseid med ugyldig format`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = URL(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )
        client.get("/utbetalinger/ydaj35").status shouldBe HttpStatusCode.BadRequest

    }

    @Test
    fun `Not found for ytelseid med datoer uten utbetaling`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = URL(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )

        mockkObject(YtelseIdUtil)
        every { YtelseIdUtil.unmarshalDateFromId("ydaj31") } returns LocalDate.now()

        withExternalServiceResponse("[]") { true }
        client.get("/utbetalinger/ydaj31").status shouldBe HttpStatusCode.NotFound

    }

    @Test
    fun `Not found for ytelseid som ikke finnes i liste over utbetalinger`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = URL(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )

        withExternalServiceResponse(spesifikkUtbetalingRespons) { true }
        every { YtelseIdUtil.unmarshalDateFromId("ydaj31") } returns LocalDate.now()
        every { YtelseIdUtil.calculateId("2023-08-24", any()) } returns "notthis"

        client.get("/utbetalinger/ydaj31").status shouldBe HttpStatusCode.NotFound
    }

    @Test
    fun `Parses utbetaltTil riktig basert på kontonummer eller metode`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = URL(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )
        withExternalServiceResponse(utbetaltTilRespons) { true }
        mockkObject(YtelseIdUtil)
        every { YtelseIdUtil.unmarshalDateFromId("testId") } returns LocalDate.now()
        every { YtelseIdUtil.calculateId("2023-08-24", any()) } returns "testId"

        client.get("/utbetalinger/testId").assert {
            status shouldBe HttpStatusCode.OK
            val json = jacksonObjectMapper().readTree(bodyAsText())
            json["ytelse"].asText() shouldBe "Andre penger"
            json["utbetaltTil"].asText() shouldBe "Annen metode"
            json["kontonummer"].asText() shouldBe "Annen metode"
            json["nettoUtbetalt"].asDouble() shouldBe 700
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

    companion object {
        @JvmStatic
        @BeforeAll
        fun mockYtelseUtil(): Unit {
            mockkObject(YtelseIdUtil)
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
