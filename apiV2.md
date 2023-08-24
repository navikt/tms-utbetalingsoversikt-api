# Endpunkter for versjon 2 av apiet

Konvensjon: felt som starter med `"__"` er kommentarere. Disse er  ikke en del av den faktiske responsen

##    /utbetalinger/siste
Detaljer om siste utbetaling \
parameter : Ingen

```json
{
  "__dato_format__": "YYYYMMdd",
  "dato": <string>
  "harUtbetaling": <boolean>
  "sisteUtbetaling": <number>,
  "ytelser": {
    "__map_description__": "map med key=ytelsesId og value=beløp",
    "__map_example__": "AAP:8990",
    "<ytelseskode>": <number>
  }
}
```

##    /utbetalinger/alle
Utbetalinger over en gitt periode, defaulter til siste tre månedr\
parameter:
* fom: fra dato på format YYYYMMdd (optional)
* tom: fra dato på format YYYYMMdd (optional)

```json
TODO
```