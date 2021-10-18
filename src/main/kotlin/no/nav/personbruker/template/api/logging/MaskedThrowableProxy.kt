package no.nav.personbruker.template.api.logging

import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.classic.spi.StackTraceElementProxy


class MaskedThrowableProxy private constructor(private val throwableProxy: IThrowableProxy) : IThrowableProxy {
    override fun getMessage(): String? {
        return MaskedLoggingEvent.mask(throwableProxy.message)
    }

    override fun getClassName(): String? {
        return throwableProxy.className
    }

    override fun getStackTraceElementProxyArray(): Array<StackTraceElementProxy?>? {
        return throwableProxy.stackTraceElementProxyArray
    }

    override fun getCommonFrames(): Int {
        return throwableProxy.commonFrames
    }

    override fun getCause(): IThrowableProxy? {
        return mask(throwableProxy.cause)
    }

    override fun getSuppressed(): Array<IThrowableProxy?>? {
        val suppressed = throwableProxy.suppressed
        val maskedSuppressed = arrayOfNulls<IThrowableProxy>(suppressed.size)
        for (i in suppressed.indices) {
            maskedSuppressed[i] = mask(suppressed[i])
        }
        return maskedSuppressed
    }

    companion object {
        @JvmStatic
        fun mask(throwableProxy: IThrowableProxy?): IThrowableProxy? {
            return if (throwableProxy == null) throwableProxy else MaskedThrowableProxy(throwableProxy)
        }
    }

}
