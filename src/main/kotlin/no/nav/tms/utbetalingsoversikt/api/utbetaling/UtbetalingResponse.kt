package no.nav.tms.utbetalingsoversikt.api.utbetaling

import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Hovedytelse
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.internal.Rettighetshaver

data class UtbetalingResponse (
    private val bruker: Rettighetshaver?,
    private val utbetalteUtbetalinger: List<Hovedytelse>,
    private val kommendeUtbetalinger: List<Hovedytelse>
)
