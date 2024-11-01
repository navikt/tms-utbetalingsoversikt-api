package no.nav.tms.utbetalingsoversikt.api.utbetaling

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.*
import org.intellij.lang.annotations.Language
import java.time.LocalDate


val eksternTestAktør = AktoerEkstern(
    aktoertype = AktoertypeEkstern.PERSON,
    identOld = "88776611",
    identNew = null,
    navn = "Navn Navnesen",
)

internal fun eksternYtelse(
    aktoerEkstern: AktoerEkstern,
    fom: String,
    nettobeløp: Double,
    ytelsesType: String,
    tom: String? = null,
    trekkbeløp: Double = 200.0,
    skattsum: Double = 10.0
) =
    YtelseEkstern(
        ytelsestype = ytelsesType,
        ytelsesperiode = PeriodeEkstern(
            fom = fom,
            tom = tom ?: fom
        ),
        ytelseNettobeloep = nettobeløp,
        rettighetshaver = aktoerEkstern,
        skattsum = skattsum,
        trekksum = trekkbeløp,
        ytelseskomponentersum = (nettobeløp.toBigDecimal()-(trekkbeløp.toBigDecimal()+skattsum.toBigDecimal())).toDouble(),
        skattListe = listOf(),
        trekkListe = listOf(),
        ytelseskomponentListe = listOf(),
        bilagsnummer = null,
        refundertForOrg = null
    )


internal fun sokoTestResponse(date: LocalDate, utbetalt: Boolean = true) = sokoTestResponse(
    dateStr = date.toString(),
    utbetalt = utbetalt
)

internal fun sokoTestResponse(
    dateStr: String = "2023-08-24",
    nettobeløp: Double = 999.5,
    utbetalt: Boolean = true,
    ytelsesListe: List<YtelseEkstern>? = null
): UtbetalingEkstern {

    return UtbetalingEkstern(
        utbetaltTil = eksternTestAktør,
        utbetalingsmetode = "Bankkontooverføring",
        utbetalingsstatus = "dummyverdi",
        posteringsdato = dateStr,
        forfallsdato = dateStr,
        utbetalingsdato = if (utbetalt) dateStr else null,
        utbetalingNettobeloep = nettobeløp,
        utbetalingsmelding = "En eller annen melding",
        utbetaltTilKonto = BankkontoEkstern(kontonummer = "9988776655443322", kontotype = "norsk bankkonto"),
        ytelseListe = ytelsesListe ?: listOf(
            eksternYtelse(eksternTestAktør, dateStr, (nettobeløp / 4), "AAP"),
            eksternYtelse(eksternTestAktør, dateStr, (nettobeløp / 4), "DAG"),
            eksternYtelse(eksternTestAktør, dateStr, (nettobeløp / 4), "FORELDRE"),
            eksternYtelse(eksternTestAktør, dateStr, (nettobeløp / 4), "SOMETHING"),

            )
    )
}


@Language("JSON")
fun nesteYtelseJson(plusDays: Long = 5) = """
      {
        "posteringsdato": "${LocalDate.now().minusDays(plusDays)}",
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

@Language("JSON")
fun tidligereYtelseJson(
    minusDays: Long,
    nettoUtbetalt: Double,
    økonomiskSosialhjelp: NettoOgTrekk,
    foreldrePenger: NettoOgTrekk,
    kontantstøtte: NettoOgTrekk,
    utbetalingsmetode: String = "Til konto",
    kontonummer: String = "1234567890",
) =
    """
      {
        "posteringsdato": "${LocalDate.now().minusDays(minusDays)}",
        "utbetaltTil": {
          "aktoertype": "PERSON",
          "ident": "123345567",
          "navn": "string"
        },
        "utbetalingNettobeloep": $nettoUtbetalt,
        "utbetalingsmelding": "string",
        "utbetalingsdato": "${LocalDate.now().minusDays(minusDays)}",
        "forfallsdato": "${LocalDate.now().minusDays(minusDays)}",
        "utbetaltTilKonto": {
          "kontonummer": "$kontonummer",
          "kontotype": "Norsk bank"
        },
        "utbetalingsmetode": "$utbetalingsmetode",
        "utbetalingsstatus": "something",
        "ytelseListe": [
          {
            "ytelsestype": "Foreldrepenger",
            "ytelsesperiode": {
              "fom": "${LocalDate.now().minusDays(minusDays)}",
              "tom": "${LocalDate.now().minusDays(minusDays)}"
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
            "trekksum": ${foreldrePenger.alleTrekk},
            "skattListe": [
              {
                "skattebeloep": 99.9
              }
            ],
            "skattsum": 1000.5,
            "ytelseNettobeloep": ${foreldrePenger.nettobeløp},
            "bilagsnummer": "84172491",
            "rettighetshaver": {
              "aktoertype": "PERSON",
              "ident": "1234567890g",
              "navn": "Navn Navnesen"
            }
          },
          {
            "ytelsestype": "Økonomisk Sosialhjelp",
            "ytelsesperiode": {
              "fom": "${LocalDate.now().minusDays(minusDays)}",
              "tom": "${LocalDate.now().minusDays(minusDays)}"
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
            "trekksum": ${økonomiskSosialhjelp.alleTrekk},
            "skattListe": [
              {
                "skattebeloep": 99.9
              }
            ],
            "skattsum": 1000.5,
            "ytelseNettobeloep": ${økonomiskSosialhjelp.nettobeløp},
            "bilagsnummer": "84172491",
            "rettighetshaver": {
              "aktoertype": "PERSON",
              "ident": "1234567890g",
              "navn": "Navn Navnesen"
            }
          },
          {
                      "ytelsestype": "Kontantstøtte",
                      "ytelsesperiode": {
                        "fom": "${LocalDate.now().minusDays(minusDays)}",
                        "tom": "${LocalDate.now().minusDays(minusDays)}"
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
                      "trekksum": ${kontantstøtte.alleTrekk},
                      "skattListe": [
                        {
                          "skattebeloep": 99.9
                        }
                      ],
                      "skattsum": 1000.5,
                      "ytelseNettobeloep": ${kontantstøtte.nettobeløp},
                      "bilagsnummer": "84172491",
                      "rettighetshaver": {
                        "aktoertype": "PERSON",
                        "ident": "1234567890g",
                        "navn": "Navn Navnesen"
                      }
                    }
        ]
      }
""".trimIndent()

internal fun Int.tidligereYtelser(
    expectedKontantstøtte: Double,
    expectedForeldrepenger: Double,
    expectedØkonomiskSosialhjelp: Double,
    expectedTrekk: Double,
    expectedUtbetalt: Double
): String {
    val ytelse = mutableListOf<String>()
    val trekkPrThing = (expectedTrekk / this) / 3
    for (i in 1..this) {
        ytelse.add(
            tidligereYtelseJson(
                minusDays = this.toLong() * 8,
                nettoUtbetalt = expectedUtbetalt,
                økonomiskSosialhjelp = NettoOgTrekk(expectedØkonomiskSosialhjelp - trekkPrThing, trekkPrThing),
                foreldrePenger = NettoOgTrekk(expectedForeldrepenger - trekkPrThing, trekkPrThing),
                kontantstøtte = NettoOgTrekk(expectedKontantstøtte - trekkPrThing, trekkPrThing),
            )
        )
    }
    return ytelse.joinToString(prefix = "[", postfix = "]", separator = ",")
}

typealias NettoOgTrekk = Pair<Double, Double>

private val NettoOgTrekk.nettobeløp
    get() = first
private val NettoOgTrekk.alleTrekk
    get() = second
