package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.Serializable

@Serializable
enum class AktoertypeEkstern {
    PERSON,
    ORGANISASJON,
    SAMHANDLER,
}
