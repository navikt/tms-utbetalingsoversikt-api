package no.nav.personbruker.template.api.logging

import ch.qos.logback.classic.spi.ThrowableProxy
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import no.nav.personbruker.template.api.logging.MaskedThrowableProxy.Companion.mask
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should not contain`
import org.junit.jupiter.api.Test
import java.io.IOException

class MaskedThrowableProxyTest {
    @Test
    fun `sjekk at maskering av fodselsnummer skjer ved kastet feil`() {
        val sensitiveException = RuntimeException(
            FNR,
            IllegalArgumentException(
                FNR,
                Exception(FNR)
            )
        )
        sensitiveException.addSuppressed(
            IllegalStateException(
                FNR,
                IOException(FNR)
            )
        )
        sensitiveException.printStackTrace()
        val sensitiveThrowableProxy = ThrowableProxy(sensitiveException)
        val maskedThrowableProxy = mask(sensitiveThrowableProxy)

        ThrowableProxyUtil.asString(sensitiveThrowableProxy) `should contain` FNR
        ThrowableProxyUtil.asString(maskedThrowableProxy) `should not contain` FNR
    }

    companion object {
        private const val FNR = "12345678901"
    }
}
