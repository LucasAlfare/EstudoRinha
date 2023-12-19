package com.lucasalfare.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

object DateSerializer : KSerializer<Date> {
  override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): Date {
    return SimpleDateFormat("yyyy-MM-dd").parse(decoder.decodeString())
  }

  override fun serialize(encoder: Encoder, value: Date) {
    encoder.encodeString(SimpleDateFormat("yyyy-MM-dd").format(value.time))
  }
}