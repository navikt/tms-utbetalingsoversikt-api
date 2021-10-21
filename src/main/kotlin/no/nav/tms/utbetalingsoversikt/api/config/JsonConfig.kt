package no.nav.tms.utbetalingsoversikt.api.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.time.LocalDate

fun jsonConfig(ignoreUnknownKeys: Boolean = false): Json {
    return Json {
        this.ignoreUnknownKeys = ignoreUnknownKeys
        this.encodeDefaults = true
    }
}

class LocalDateSerializer: KSerializer<LocalDate> {
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

class BigDecimalSerializer: KSerializer<BigDecimal> {
    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal.valueOf(decoder.decodeDouble())
    }

    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "no.nav.tms.utbetalingsoversikt.api.config.BigDecimalSerializer",
        kind = PrimitiveKind.DOUBLE
    )

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        return encoder.encodeDouble(value.toDouble())
    }

}
