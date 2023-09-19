package no.nav.tms.utbetalingsoversikt.api.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

fun jsonConfig() = Json {
    this.ignoreUnknownKeys = true
    this.encodeDefaults = true
}


class LocalDateSerializer : KSerializer<LocalDate> {
    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }

    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "no.nav.tms.utbetalingsoversikt.api.config.LocalDateSerializer",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }

}

class BigDecimalSerializer : KSerializer<BigDecimal> {
    override fun deserialize(decoder: Decoder): BigDecimal {
        return decoder.decodeString().toBigDecimal()
    }

    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "no.nav.tms.utbetalingsoversikt.api.v2",
        kind = PrimitiveKind.FLOAT
    )

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeDouble(value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toDouble())
    }

}

class KontonummerSerializer : KSerializer<String?> {
    override fun deserialize(decoder: Decoder): String = decoder.decodeString()

    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "no.nav.tms.utbetalingsoversikt.api.v2",
        kind = PrimitiveKind.STRING
    )
    override fun serialize(encoder: Encoder, value: String?) {
        encoder.encodeString(value?.let { "xxxxxx${it.substring(it.length - 5)}" } ?: "----")
    }


}


