package no.nav.tms.utbetalingsoversikt.api.utbetaling


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
import io.ktor.server.auth.*
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
import no.nav.tms.token.support.idporten.sidecar.mock.idPortenMock
import no.nav.tms.token.support.tokendings.exchange.TokendingsService
import no.nav.tms.token.support.tokenx.validation.mock.LevelOfAssurance
import no.nav.tms.token.support.tokenx.validation.mock.tokenXMock
import no.nav.tms.utbetalingsoversikt.api.config.createUrl
import no.nav.tms.utbetalingsoversikt.api.config.jsonConfig
import no.nav.tms.utbetalingsoversikt.api.config.utbetalingApi
import no.nav.tms.utbetalingsoversikt.api.ytelse.SokosUtbetalingConsumer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URL
import java.time.LocalDate


class UtbetalingRoutesTest {
    private val objectMapper = jacksonObjectMapper()
    private val testHost = "https://utbetaling.ekstern.test"
    private val tokendingsMockk = mockk<TokendingsService>().also {
        coEvery {
            it.exchangeToken(any(), any())
        } returns "<dummytoken>"
    }
    private val spesifikkUtbetalingRespons = File("src/test/resources/utbetaling_detalj_test.json").readText()
    private val utbetaltTilRespons = File("src/test/resources/utbetaling_detalj_utbetalttil_test.json").readText()

    @Test
    fun `henter alle utbetalinger i kronologisk rekkefølge`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = createUrl(testHost),
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
                baseUrl = createUrl(testHost),
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


        val status = client.get("/internal/isAlive")

        println(status)


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
    fun `returnerer siste og neste utbetalinger`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = createUrl(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )
        val tidligereUtbetalingerJson = File("src/test/resources/siste_utbetaling_test.json").readText()

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
            val expectedFom = LocalDate.now().minusMonths(3)
            val expectedTom = LocalDate.now().plusMonths(3).plusDays(1)
            val actualFom = LocalDate.parse(fomtom["periode"]["fom"].asText())
            val actualTom = LocalDate.parse(fomtom["periode"]["tom"].asText())
            actualFom == expectedFom && actualTom == expectedTom
        }

        client.get("/utbetalinger/siste").assert {
            status shouldBe HttpStatusCode.OK
            val response = objectMapper.readTree(bodyAsText())

            response["hasUtbetaling"].asBoolean() shouldBe true
            response["hasKommende"].asBoolean() shouldBe true
            response["sisteUtbetaling"].apply {
                require(this != null && !this.isNull) { "sisteUtbetaling har ikke innhold" }
                this["utbetaling"].asInt() shouldBe 3788
                this["dato"].asText() shouldBe "2023-11-10"
                this["ytelse"].asText() shouldBe "Dagpenger"
                withClue("id ikke tilstede i respons") {
                    this["id"].isNull shouldNotBe true
                }
                this["kontonummer"].asText() shouldBe "xxxxx39876"
            }

            response["kommende"].apply {
                require(this != null && !this.isNull) { "kommendeobjekt har ikke innhold" }
                this["utbetaling"].asInt() shouldBe 3788
                this["ytelse"].asText() shouldBe "Dagpenger"
                withClue("id ikke tilstede i respons") {
                    this["id"].isNull shouldNotBe true
                }
                this["kontonummer"].asText() shouldBe "xxxxx55444"
                LocalDate.parse(this["dato"].asText()) shouldBe LocalDate.now().plusDays(1)
            }
        }
    }

    @Test
    fun `returnerer siste utbetaling og tomt for neste utbetaling`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = createUrl(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )
        val tidligereUtbetalingerJson = File("src/test/resources/siste_utbetaling_test.json").readText()

        withExternalServiceResponse(tidligereUtbetalingerJson) {
            val fomtom = objectMapper.readTree(call.receiveText())
            val expectedFom = LocalDate.now().minusMonths(3)
            val expectedTom = LocalDate.now().plusMonths(3).plusDays(1)
            val actualFom = LocalDate.parse(fomtom["periode"]["fom"].asText())
            val actualTom = LocalDate.parse(fomtom["periode"]["tom"].asText())
            actualFom == expectedFom && actualTom == expectedTom
        }

        client.get("/utbetalinger/siste").assert {
            status shouldBe HttpStatusCode.OK
            val response = objectMapper.readTree(bodyAsText())
            response["hasUtbetaling"].asBoolean() shouldBe true
            response["hasKommende"].asBoolean() shouldBe false
            response["sisteUtbetaling"].isNull shouldBe false
            response["kommende"].isNull shouldBe true
        }
    }

    @Test
    fun `returnerer neste utbetaling og tomt for siste utbetaling`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = createUrl(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )
        withExternalServiceResponse(
            body = """[
          ${nesteYtelseJson(1)},    
          ${nesteYtelseJson(15)},    
          ${nesteYtelseJson(20)}    
           ]
        """.trimIndent()
        ) {
            val fomtom = objectMapper.readTree(call.receiveText())
            val expectedFom = LocalDate.now().minusMonths(3)
            val expectedTom = LocalDate.now().plusMonths(3).plusDays(1)
            val actualFom = LocalDate.parse(fomtom["periode"]["fom"].asText())
            val actualTom = LocalDate.parse(fomtom["periode"]["tom"].asText())
            actualFom == expectedFom && actualTom == expectedTom
        }

        client.get("/utbetalinger/siste").assert {
            status shouldBe HttpStatusCode.OK
            val response = objectMapper.readTree(bodyAsText())

            response["hasUtbetaling"].asBoolean() shouldBe false
            response["hasKommende"].asBoolean() shouldBe true
            response["sisteUtbetaling"].isNull shouldBe true
            response["kommende"].isNull shouldBe false
        }
    }

    @Test
    fun `returner tomt objekt hvis det ikke finnes noen utbetalinger de siste 3 månedene`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = createUrl(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )

        withExternalServiceResponse("[]")
        client.get("/utbetalinger/siste").assert {
            status shouldBe HttpStatusCode.OK
            objectMapper.readTree(bodyAsText()).apply {
                this["hasUtbetaling"].asBoolean() shouldBe false
                this["hasKommende"].asBoolean() shouldBe false
                this["sisteUtbetaling"].isNull shouldBe true
                this["kommende"].isNull shouldBe true
            }
        }
    }

    @Test
    fun `returnerer detaljertinfo om utbetaling for spesifikk utbetaling av en ytelse`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = createUrl(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )
        withExternalServiceResponse(spesifikkUtbetalingRespons)
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
                baseUrl = createUrl(testHost),
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
                baseUrl = createUrl(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )

        mockkObject(YtelseIdUtil)
        every { YtelseIdUtil.unmarshalDateFromId("ydaj31") } returns LocalDate.now()

        withExternalServiceResponse("[]")
        client.get("/utbetalinger/ydaj31").status shouldBe HttpStatusCode.NotFound

    }

    @Test
    fun `Not found for ytelseid som ikke finnes i liste over utbetalinger`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = createUrl(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )

        withExternalServiceResponse(spesifikkUtbetalingRespons)
        every { YtelseIdUtil.unmarshalDateFromId("ydaj31") } returns LocalDate.now()
        every { YtelseIdUtil.calculateId("2023-08-24", any()) } returns "notthis"

        client.get("/utbetalinger/ydaj31").status shouldBe HttpStatusCode.NotFound
    }

    @Test
    fun `Parses utbetaltTil riktig basert på kontonummer eller metode`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = createUrl(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )
        withExternalServiceResponse(utbetaltTilRespons)
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

    @Test
    fun `Retunere 503 når baksystemfeiler`() = testApplication {
        testApi(
            (SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = createUrl(testHost),
                sokosUtbetaldataClientId = "test:client:id",
                tokendingsService = tokendingsMockk
            ))
        )
        externalServices {
            hosts(testHost) {
                routing {
                    route("hent-utbetalingsinformasjon/ekstern") {
                        post {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }

                }
            }
        }
        client.get("/utbetalinger/siste").status shouldBe HttpStatusCode.ServiceUnavailable
    }


    @Test
    fun `henter alle utbetalinger for tokenx`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = createUrl(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )

        val tidligereUtbetalingerJson = File("src/test/resources/alle_utbetalinger_tidligere_default.json").readText()

        withExternalServiceResponse(
            """
          ${tidligereUtbetalingerJson.substring(0, tidligereUtbetalingerJson.lastIndexOf("]"))},
          ${nesteYtelseJson(20)}    
           ]
            
        """.trimIndent()
        ) { true }

        client.get("/utbetalinger/ssr/alle").assert {
            status shouldBe HttpStatusCode.OK
        }
    }

    @Test
    fun `henter siste og neste utbetalinger for tokenx`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = createUrl(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )
        val tidligereUtbetalingerJson = File("src/test/resources/siste_utbetaling_test.json").readText()

        withExternalServiceResponse(
            body = """
          ${tidligereUtbetalingerJson.substring(0, tidligereUtbetalingerJson.lastIndexOf("]"))},
          ${nesteYtelseJson(20)}    
           ]
        """.trimIndent()
        ) {
            val fomtom = objectMapper.readTree(call.receiveText())
            val expectedFom = LocalDate.now().minusMonths(3)
            val expectedTom = LocalDate.now().plusMonths(3).plusDays(1)
            val actualFom = LocalDate.parse(fomtom["periode"]["fom"].asText())
            val actualTom = LocalDate.parse(fomtom["periode"]["tom"].asText())
            actualFom == expectedFom && actualTom == expectedTom
        }

        client.get("/utbetalinger/ssr/siste").assert {
            status shouldBe HttpStatusCode.OK
        }
    }

    @Test
    fun `henter detaljert info om utbetaling for tokenx`() = testApplication {
        testApi(
            SokosUtbetalingConsumer(
                client = sokosHttpClient,
                baseUrl = createUrl(testHost),
                tokendingsService = tokendingsMockk,
                sokosUtbetaldataClientId = "test:client:id"
            )
        )
        withExternalServiceResponse(spesifikkUtbetalingRespons)
        mockkObject(YtelseIdUtil)
        every { YtelseIdUtil.unmarshalDateFromId("<mockID>") } returns LocalDate.now()
        every { YtelseIdUtil.calculateId("2023-08-24", any()) } returns "<mockID>"

        client.get("/utbetalinger/ssr/<mockID>").assert {
            status shouldBe HttpStatusCode.OK
        }
    }

    private fun ApplicationTestBuilder.withExternalServiceResponse(
        body: String,
        replyIf: suspend RoutingContext.() -> Boolean = { true }
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
                authentication {
                    idPortenMock {
                        setAsDefault = true
                        staticLevelOfAssurance = HIGH
                        staticUserPid = "12345"
                        alwaysAuthenticated = true
                    }
                    tokenXMock {
                        setAsDefault = false
                        staticLevelOfAssurance = LevelOfAssurance.SUBSTANTIAL
                        staticUserPid = "12345"
                        alwaysAuthenticated = true
                    }
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
