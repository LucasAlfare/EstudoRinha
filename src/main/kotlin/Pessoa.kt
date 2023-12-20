@file:UseSerializers(
  UUIDSerializer::class
)

package com.lucasalfare

import com.lucasalfare.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

/**
 * Auxiliary model to automatic deserialize incoming JSON requests.
 */
@Serializable
data class PessoaDTO(
  val nome: String?,
  val apelido: String?,
  val nascimento: String?,
  val stack: Array<String>?
) {

  /**
   * Validates the properties on instantiation.
   */
  init {
    // checks for nulls
    requireNotNull(nome)
    requireNotNull(apelido)
    requireNotNull(nascimento)

    // checks for challenge requirements
    require(nome.length in 1..100)
    require(apelido.length in 1..32)

    // custom checks for date format
    require(validateNascimento(nascimento))

    // checks for each item of the deserialized list
    stack?.forEach { require(it.length in 1..32) }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as PessoaDTO

    if (nome != other.nome) return false
    if (apelido != other.apelido) return false
    if (nascimento != other.nascimento) return false
    if (stack != null) {
      if (other.stack == null) return false
      if (!stack.contentEquals(other.stack)) return false
    } else if (other.stack != null) return false

    return true
  }

  override fun hashCode(): Int {
    var result = nome?.hashCode() ?: 0
    result = 31 * result + (apelido?.hashCode() ?: 0)
    result = 31 * result + (nascimento?.hashCode() ?: 0)
    result = 31 * result + (stack?.contentHashCode() ?: 0)
    return result
  }
}

/**
 * The persistable model of "Pessoa".
 */
@Serializable
data class Pessoa(
  val id: UUID,
  val nome: String,
  val apelido: String,
  val nascimento: String,
  val stack: Array<String>?
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Pessoa

    if (id != other.id) return false
    if (nome != other.nome) return false
    if (apelido != other.apelido) return false
    if (nascimento != other.nascimento) return false
    if (stack != null) {
      if (other.stack == null) return false
      if (!stack.contentEquals(other.stack)) return false
    } else if (other.stack != null) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + nome.hashCode()
    result = 31 * result + apelido.hashCode()
    result = 31 * result + nascimento.hashCode()
    result = 31 * result + (stack?.contentHashCode() ?: 0)
    return result
  }
}

/**
 * Scratch function to check and validate if string matches the desired fixed format.
 *
 * format: [yyyy-MM-dd]
 *
 * Is [StringBuilder] useful in order to avoid literal strings allocations?
 */
private fun validateNascimento(nascimento: String): Boolean {
  // dates should be always in fixed size, if not, just finishes
  if (nascimento.length != 10) return false

  val auxBuilder = StringBuilder()

  // checks if "year" exists in first 4 chars
  if (
    auxBuilder
      .append(nascimento[0])
      .append(nascimento[1])
      .append(nascimento[2])
      .append(nascimento[3])
      .toString()
      .toIntOrNull() == null
  ) return false

  // check for the following separator
  if (nascimento[4] != '-') return false

  auxBuilder.clear()

  // checks if "month" exists in following 2 chars
  if (
    auxBuilder
      .append(nascimento[5])
      .append(nascimento[6])
      .toString()
      .toIntOrNull() == null
  ) return false

  // check for the second separator
  if (nascimento[7] != '-') return false

  auxBuilder.clear()

  // last check, for the "day"
  return auxBuilder
    .append(nascimento[8])
    .append(nascimento[9])
    .toString()
    .toIntOrNull() != null
}