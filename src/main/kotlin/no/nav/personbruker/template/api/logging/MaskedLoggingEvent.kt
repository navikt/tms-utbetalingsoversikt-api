package no.nav.personbruker.template.api.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.classic.spi.LoggerContextVO
import no.nav.personbruker.template.api.logging.MaskedThrowableProxy.Companion.mask
import org.slf4j.Marker

class MaskedLoggingEvent internal constructor(private val iLoggingEvent: ILoggingEvent) : ILoggingEvent {
    override fun getThreadName(): String? {
        return iLoggingEvent.threadName
    }

    override fun getLevel(): Level? {
        return iLoggingEvent.level
    }

    override fun getMessage(): String? {
        return iLoggingEvent.message
    }

    override fun getArgumentArray(): Array<Any>? {
        return iLoggingEvent.argumentArray
    }

    override fun getFormattedMessage(): String? {
        return mask(iLoggingEvent.formattedMessage)
    }

    override fun getLoggerName(): String? {
        return iLoggingEvent.loggerName
    }

    override fun getLoggerContextVO(): LoggerContextVO? {
        return iLoggingEvent.loggerContextVO
    }

    override fun getThrowableProxy(): IThrowableProxy? {
        return mask(iLoggingEvent.throwableProxy)
    }

    override fun getCallerData(): Array<StackTraceElement?>? {
        return iLoggingEvent.callerData
    }

    override fun hasCallerData(): Boolean {
        return iLoggingEvent.hasCallerData()
    }

    override fun getMarker(): Marker? {
        return iLoggingEvent.marker
    }

    override fun getMDCPropertyMap(): Map<String?, String?>? {
        return iLoggingEvent.mdcPropertyMap.mapValues { mask(it.value) }
    }

    override fun getMdc(): Map<String?, String?>? {
        return iLoggingEvent.mdcPropertyMap.mapValues { mask(it.value) }
    }

    override fun getTimeStamp(): Long {
        return iLoggingEvent.timeStamp
    }

    override fun prepareForDeferredProcessing() {
        iLoggingEvent.prepareForDeferredProcessing()
    }

    companion object {
        fun mask(string: String?): String? {
            return string?.replace("(^|\\W)\\d{11}(?=$|\\W)".toRegex(), "$1***********")
        }
    }

}
