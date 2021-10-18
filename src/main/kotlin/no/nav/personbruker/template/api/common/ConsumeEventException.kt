package no.nav.personbruker.template.api.common

import java.lang.Exception

class ConsumeEventException(message: String, cause: Throwable) : Exception(message, cause)
