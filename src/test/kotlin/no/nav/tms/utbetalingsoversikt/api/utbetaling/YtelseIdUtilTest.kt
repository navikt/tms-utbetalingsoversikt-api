package no.nav.tms.utbetalingsoversikt.api.utbetaling

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.EksternModelObjectMother
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.PeriodeEkstern
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class YtelseIdUtilTest {

    @Test
    fun `kan hente posteringsdato fra id`() {
        val posteringsdato = LocalDate.now()

        val ytelse = EksternModelObjectMother.giveMeYtelseEkstern()

        val id = YtelseIdUtil.calculateId(posteringsdato.toString(), ytelse)

        YtelseIdUtil.unmarshalDateFromId(id) shouldBe posteringsdato
    }

    @Test
    fun `endring i posteringsdato endrer id`() {
        val postering1 = LocalDate.now().toString()
        val postering2 = LocalDate.now().minusDays(1).toString()

        val ytelse = EksternModelObjectMother.giveMeYtelseEkstern()

        val id1 = YtelseIdUtil.calculateId(postering1, ytelse)
        val id2 = YtelseIdUtil.calculateId(postering2, ytelse)

        id1 shouldNotBe id2

        val (datePart1, contentPart1) = id1.split("-")
        val (datePart2, contentPart2) = id2.split("-")

        datePart1 shouldNotBe datePart2
        contentPart1 shouldBe contentPart2
    }

    @Test
    fun `endring i bilagsnummer endrer id`() {
        val postering = LocalDate.now().toString()

        val ytelse1 = EksternModelObjectMother.giveMeYtelseEkstern(bilagsnummer = "123")
        val ytelse2 = EksternModelObjectMother.giveMeYtelseEkstern(bilagsnummer = "456")

        val id1 = YtelseIdUtil.calculateId(postering, ytelse1)
        val id2 = YtelseIdUtil.calculateId(postering, ytelse2)

        id1 shouldNotBe id2

        val (datePart1, contentPart1) = id1.split("-")
        val (datePart2, contentPart2) = id2.split("-")

        datePart1 shouldBe datePart2
        contentPart1 shouldNotBe contentPart2
    }

    @Test
    fun `endring i ytelsetype endrer id`() {
        val postering = LocalDate.now().toString()

        val ytelse1 = EksternModelObjectMother.giveMeYtelseEkstern(ytelsestype = "Type 1")
        val ytelse2 = EksternModelObjectMother.giveMeYtelseEkstern(ytelsestype = "Type 2")

        val id1 = YtelseIdUtil.calculateId(postering, ytelse1)
        val id2 = YtelseIdUtil.calculateId(postering, ytelse2)

        id1 shouldNotBe id2

        val (datePart1, contentPart1) = id1.split("-")
        val (datePart2, contentPart2) = id2.split("-")

        datePart1 shouldBe datePart2
        contentPart1 shouldNotBe contentPart2
    }

    @Test
    fun `endring i type ytelseskomponenter endrer id`() {
        val postering = LocalDate.now().toString()

        val ytelsesKomponenter1 = listOf(
            EksternModelObjectMother.giveMeYtelsesKomponentEkstern(komponenttype = "Type 1"),
            EksternModelObjectMother.giveMeYtelsesKomponentEkstern(komponenttype = "Type 2")
        )

        val ytelsesKomponenter2 = listOf(
            EksternModelObjectMother.giveMeYtelsesKomponentEkstern(komponenttype = "Type 1"),
            EksternModelObjectMother.giveMeYtelsesKomponentEkstern(komponenttype = "Type 1")
        )

        val ytelse1 = EksternModelObjectMother.giveMeYtelseEkstern(ytelseskomponentListe = ytelsesKomponenter1)
        val ytelse2 = EksternModelObjectMother.giveMeYtelseEkstern(ytelseskomponentListe = ytelsesKomponenter2)

        val id1 = YtelseIdUtil.calculateId(postering, ytelse1)
        val id2 = YtelseIdUtil.calculateId(postering, ytelse2)

        id1 shouldNotBe id2

        val (datePart1, contentPart1) = id1.split("-")
        val (datePart2, contentPart2) = id2.split("-")

        datePart1 shouldBe datePart2
        contentPart1 shouldNotBe contentPart2
    }

    @Test
    fun `endring i belop for ytelseskomponenter endrer id`() {
        val postering = LocalDate.now().toString()

        val ytelsesKomponenter1 = listOf(
            EksternModelObjectMother.giveMeYtelsesKomponentEkstern(komponentbeloep = 123.0),
            EksternModelObjectMother.giveMeYtelsesKomponentEkstern(komponentbeloep = 456.0)
        )

        val ytelsesKomponenter2 = listOf(
            EksternModelObjectMother.giveMeYtelsesKomponentEkstern(komponentbeloep = 123.0),
            EksternModelObjectMother.giveMeYtelsesKomponentEkstern(komponentbeloep = 123.0)
        )

        val ytelse1 = EksternModelObjectMother.giveMeYtelseEkstern(ytelseskomponentListe = ytelsesKomponenter1)
        val ytelse2 = EksternModelObjectMother.giveMeYtelseEkstern(ytelseskomponentListe = ytelsesKomponenter2)

        val id1 = YtelseIdUtil.calculateId(postering, ytelse1)
        val id2 = YtelseIdUtil.calculateId(postering, ytelse2)

        id1 shouldNotBe id2

        val (datePart1, contentPart1) = id1.split("-")
        val (datePart2, contentPart2) = id2.split("-")

        datePart1 shouldBe datePart2
        contentPart1 shouldNotBe contentPart2
    }

    @Test
    fun `rekkefølge på i ytelseskomponenter endrer ikke id`() {
        val postering = LocalDate.now().toString()

        val ytelsesKomponenter1 = listOf(
            EksternModelObjectMother.giveMeYtelsesKomponentEkstern(komponenttype = "Type 1", komponentbeloep = 123.0),
            EksternModelObjectMother.giveMeYtelsesKomponentEkstern(komponenttype = "Type 2", komponentbeloep = 456.0)
        )

        val ytelsesKomponenter2 = listOf(
            EksternModelObjectMother.giveMeYtelsesKomponentEkstern(komponenttype = "Type 2", komponentbeloep = 456.0),
            EksternModelObjectMother.giveMeYtelsesKomponentEkstern(komponenttype = "Type 1", komponentbeloep = 123.0)
        )

        val ytelse1 = EksternModelObjectMother.giveMeYtelseEkstern(ytelseskomponentListe = ytelsesKomponenter1)
        val ytelse2 = EksternModelObjectMother.giveMeYtelseEkstern(ytelseskomponentListe = ytelsesKomponenter2)

        val id1 = YtelseIdUtil.calculateId(postering, ytelse1)
        val id2 = YtelseIdUtil.calculateId(postering, ytelse2)

        id1 shouldBe id2
    }

    @Test
    fun `ulike perioder gir ulike id-er`() {
        val postering = LocalDate.now().toString()

        val ytelse1 = EksternModelObjectMother.giveMeYtelseEkstern(
            ytelsesperiode = PeriodeEkstern("2025-12-01", "2025-12-07")
        )
        val ytelse2 = EksternModelObjectMother.giveMeYtelseEkstern(
            ytelsesperiode = PeriodeEkstern("2025-12-08", "2025-12-14")
        )

        val id1 = YtelseIdUtil.calculateId(postering, ytelse1)
        val id2 = YtelseIdUtil.calculateId(postering, ytelse2)

        id1 shouldNotBe id2
    }
}
