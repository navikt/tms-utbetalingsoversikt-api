package no.nav.tms.utbetalingsoversikt.api.ytelse

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.idporten.user.IdportenUser
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.RolleEkstern.UTBETALT_TIL
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.UtbetalingEkstern
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.transformer.HovedytelseTransformer
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HovedytelseServiceTest {

    private val consumer: SokosUtbetalingConsumer = mockk()

    private val hovedytelseService = HovedytelseService(consumer)

    init {
        mockkObject(HovedytelseTransformer)
    }

    @AfterAll
    fun cleanUp() {
        clearMocks(consumer)
        unmockkObject(HovedytelseTransformer)
    }

    @Test
    fun `should return list of transformed hovedytelse`() {

        val user: IdportenUser = mockk()
        val fom: LocalDate = mockk()
        val tom: LocalDate = mockk()

        val eksternalUtbetaling1: UtbetalingEkstern = mockk()
        val eksternalUtbetaling2: UtbetalingEkstern = mockk()

        val rawData: List<UtbetalingEkstern> = listOf(eksternalUtbetaling1, eksternalUtbetaling2)

        val transformedData1 = listOf(mockk<Hovedytelse>(), mockk())
        val transformedData2 = listOf(mockk<Hovedytelse>(), mockk(), mockk())

        coEvery { consumer.fetchUtbetalingsInfo(user, fom, tom, UTBETALT_TIL) } returns rawData
        every { HovedytelseTransformer.toHovedYtelse(eksternalUtbetaling1) } returns transformedData1
        every { HovedytelseTransformer.toHovedYtelse(eksternalUtbetaling2) } returns transformedData2

        val result = runBlocking {
            hovedytelseService.getHovedytelserBetaltTilBruker(user, fom, tom)
        }

        result.size `should be equal to` 5

        coVerify(exactly = 1) { consumer.fetchUtbetalingsInfo(any(), any(), any(), any()) }
        verify(exactly = 2) { HovedytelseTransformer.toHovedYtelse(any()) }
    }
}
