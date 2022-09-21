package no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AktoerEkstern constructor(
    val aktoertype: AktoertypeEkstern,
    @SerialName("aktoerId") val identOld: String? = null,
    @SerialName("ident") val identNew: String? = null,
    val navn: String? = null,
) {
    init {
        require(identOld != null || identNew != null) {
            "Trenger enten felt 'aktoerId' eller 'ident'"
        }
    }

    val ident: String get() = identNew?: identOld?: throw IllegalStateException("AktoerEkstern manglet ident")
}
