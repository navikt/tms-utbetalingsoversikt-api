package no.nav.personbruker.template.api.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.AppenderBase

class MaskingAppender : AppenderBase<ILoggingEvent>() {
    private var appender: Appender<ILoggingEvent>? = null

    override fun append(iLoggingEvent: ILoggingEvent) {
        appender?.doAppend(MaskedLoggingEvent(iLoggingEvent))
    }

    fun setAppender(appender: Appender<ILoggingEvent>?) {
        this.appender = appender
    }
}
