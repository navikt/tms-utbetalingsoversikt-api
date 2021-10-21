package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern

object KontonummerTransformer {
    private const val ANTALL_TEGN_SYNLIGE_KONTONUMMER = 5
    private const val SENSUR_TEGN_KONTONUMMER = "x"

    fun determineKontonummerVerdi(utbetaling: UtbetalingEkstern): String {
        return if (harKontonummer(utbetaling)) {
            sensurerKontonummer(utbetaling.utbetaltTilKonto!!.kontonummer)
        } else {
            utbetaling.utbetalingsmetode
        }
    }


    private fun harKontonummer(utbetaling: UtbetalingEkstern): Boolean {
        return utbetaling.utbetaltTilKonto != null && utbetaling.utbetaltTilKonto.kontonummer.isNotBlank()
    }

    private fun sensurerKontonummer(rawKontonummer: String): String {
        return if (rawKontonummer.length <= ANTALL_TEGN_SYNLIGE_KONTONUMMER) {
            rawKontonummer
        } else {
            val kontonummer = rawKontonummer.removeWhitespace()

            return maskAllButFinalCharacters(kontonummer)
        }
    }

    private fun maskAllButFinalCharacters(toMask: String, mask: String = SENSUR_TEGN_KONTONUMMER, numberUnmasked: Int = ANTALL_TEGN_SYNLIGE_KONTONUMMER): String {
        val numberMaskedChars = toMask.length - numberUnmasked

        return "${mask.repeat(numberMaskedChars)}${toMask.substring(numberMaskedChars)}"
    }

    private fun String.removeWhitespace() = replace(" ", "")
}
