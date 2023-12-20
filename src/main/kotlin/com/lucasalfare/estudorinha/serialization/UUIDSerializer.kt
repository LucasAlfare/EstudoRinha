package com.lucasalfare.estudorinha.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

/**
 * Serializer for UUID objects implementing the KSerializer interface.
 */
object UUIDSerializer : KSerializer<UUID> {

  /**
   * Descriptor for the UUIDSerializer, defining it as a string primitive kind.
   */
  override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

  /**
   * Deserialize method to convert a string representation into a UUID object.
   *
   * @param decoder The decoder to read the string from.
   * @return The deserialized UUID object.
   */
  override fun deserialize(decoder: Decoder): UUID {
    return UUID.fromString(decoder.decodeString())
  }

  /**
   * Serialize method to convert a UUID object into its string representation.
   *
   * @param encoder The encoder to write the string to.
   * @param value The UUID object to be serialized.
   */
  override fun serialize(encoder: Encoder, value: UUID) {
    encoder.encodeString(value.toString())
  }
}