package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable

@Serializable
enum class AktoertypeEsktern {
    PERSON,
    ORGANISASJON,
    SAMHANDLER,
}
