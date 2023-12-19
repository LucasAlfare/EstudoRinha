@file:Suppress("ArrayInDataClass")
@file:UseSerializers(
  DateSerializer::class,
  UUIDSerializer::class
)

package com.lucasalfare

import com.lucasalfare.serialization.DateSerializer
import com.lucasalfare.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

@Serializable
data class PessoaDTO(
  val nome: String?,
  val apelido: String?,
  val nascimento: Date?,
  val stack: Array<String>?
)

@Serializable
data class Pessoa(
  val id: UUID,
  val nome: String,
  val apelido: String,
  val nascimento: Date,
  val stack: Array<String>?
)