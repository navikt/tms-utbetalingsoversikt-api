# tms-utbetalingsoversikt-api
Henter og transformerer utbetalinger fra sokos-utbetaling.


## api v2

 `/utbetalinger/siste` \
Detaljer om siste utbetaling \
parameter : Ingen

```json
{
  "__dato_format__": "yyyyMMdd",
  "dato": "string",
  "harUtbetaling": "boolean",
  "sisteUtbetaling": "number",
  "ytelser": {
    "__map_description__": "map med key=ytelsesId og value=beløp",
    "__map_example__": "AAP:8990",
    "<ytelseskode>": "number"
  }
}
```

`/utbetalinger/alle`\
Utbetalinger over en gitt periode, defaulter til siste tre månedr\
parameter:
* fom: fra dato på format yyyyMMdd (optional)
* tom: fra dato på format yyyyMMdd (optional)

```json
{
  "__neste_content": "liste av ytelse",
  "neste": [
    {
      "id": "string",
      "beløp": "2-point-float-number",
      "dato": "date",
      "ytelse": "string"
    },
    {
      "__example__": "ytelse",
      "id": "h9kajfhau8",
      "beløp": 4400.94,
      "dato": "2023-02-24",
      "ytelse": "2023-02-24"
    }
  ],
  "__tidligere_content__": "liste av tidligere utbetalinger grupert på måned og år",
  "tidligere": [
    {
      "år": "number",
      "måned": "number",
      "__utbetalinger_content__": "liste av ytelse, se neste-objekt for eksempel",
      "utbetalinger": []
    },
    {
      "__eksempel__": "utbetalinger pr måned og år",
      "år": 2023,
      "måned": 2,
      "utbetalinger": [
        {
          "id": "4bd4-67777b5495719748",
          "beløp": 2600.87,
          "dato": "2023-02-24",
          "ytelse": "2023-02-24"
        },
        {
          "id": "4bd4-3b2db7f528db6965",
          "beløp": 2311.13,
          "dato": "2023-02-24",
          "ytelse": "Økonomisk sosialhjelp"
        }
      ]
    }
  ],
  "utbetalingerIPeriode": {
    "harUtbetalinger": "boolean",
    "brutto": "2-point-float-number representert i string",
    "netto": "2-point-float-number representert i string",
    "trekk": "2-point-float-number representert i string",

    "__example_brutto__": "45612.87",
    "__example_netto__": "33612.87",
    "__example_trekk__": "12000",

    "__content_ytelser__": "liste av ytelser med navn og samlet beløp for periode",

    "ytelser": [
      {
        "ytelse": "string",
        "beløp": "2-point-float-number representert i string"
      },
      {
        "__example__": "oppsummert ytelse",
        "ytelse": "Foreldrepenger",
        "beløp": "18004.35"
      }
    ]
  }
}
```

`/utbetalinger/{ytelseId}`

```json
{
  "ytelse": "Navn på ytelse",
  "erUtbetalt": "true/false",
  "ytelsePeriode": {
    "fom": "dato",
    "tom": "dato"
  },
  "ytelseDato": "utbetaltdato/forfallsdato ",
  "__kontonummer_desc__": "Samme verdi som 'utbetaltTil'. Deprekeres.",
  "kontonummer": "xxxxxx98765",
  "__utbetaltTil_desc__":  "Utbetalt til vil enten være et maskert kontonummer, eller annen utbetalingsmetode.",
  "utbetaltTil": "xxxxxx98765",
  "underytelser": [
    {
      "beskrivelse": "Grunnbeløp",
      "__satstype_desc__": "string eller null",
      "satstype": "Something",
      "sats": 100,
      "antall": 3,
      "__beløp_desc__": "samlet beløp(sats*antall)",
      "beløp": 300
    }
  ],
  "trekk": [
    {
      "type": "Skatt",
      "beløp": 50
    },
    {
      "type": "Annet trekk",
      "beløp": 50
    }
  ],
  "melding": "Melding som handler om utbetalin av ytelse",
  "__bruttoUtbetalt_desc__": "sum av utbetalinger fra alle underytelser",
  "bruttoUtbetalt": 300,
  "__nettoUtbetalt_desc__": "bruttoUtbetalt minus sum av alle trekk",
  "nettoUtbetalt": 200
}
```
