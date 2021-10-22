package no.nav.tms.utbetalingsoversikt.api.utbetaling

import kotlinx.serialization.Serializable
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Rettighetshaver

@Serializable
data class UtbetalingResponse(
    private val bruker: Rettighetshaver?,
    private val utbetalteUtbetalinger: List<Hovedytelse>,
    private val kommendeUtbetalinger: List<Hovedytelse>
)
