package no.nav.personbruker.template.api.common

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.Test

internal class IdentityClaimTest {

    @Test
    fun `should convert valid strings to enum`() {
        IdentityClaim.fromClaimName("pid") `should be equal to` IdentityClaim.PID
        IdentityClaim.fromClaimName("PID") `should be equal to` IdentityClaim.PID
        IdentityClaim.fromClaimName("sub") `should be equal to` IdentityClaim.SUBJECT
        IdentityClaim.fromClaimName("SUB") `should be equal to` IdentityClaim.SUBJECT
    }

    @Test
    fun `should throw exception if invalid value is received`() {
        invoking {
            IdentityClaim.fromClaimName("")
        } `should throw` IllegalArgumentException::class
    }

}
