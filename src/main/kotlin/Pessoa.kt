@file:UseSerializers(
  UUIDSerializer::class
)

package com.lucasalfare

import com.lucasalfare.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

/**
 * Data Transfer Object (DTO) representing a person with optional technology stack.
 *
 * @property nome The name of the person.
 * @property apelido The nickname of the person.
 * @property nascimento The date of birth of the person (in the format [yyyy-MM-dd]).
 * @property stack An array of technologies associated with the person.
 *
 * @throws IllegalArgumentException if any of the validation checks fail during object initialization.
 */
@Serializable
data class PessoaDTO(
  val nome: String?,
  val apelido: String?,
  val nascimento: String?,
  val stack: Array<String>?
) {

  init {
    // Checks for null values.
    requireNotNull(nome)
    requireNotNull(apelido)
    requireNotNull(nascimento)

    // Checks for challenge requirements.
    require(nome.length in 1..100)
    require(apelido.length in 1..32)

    // Custom checks for date format.
    require(validateNascimento(nascimento))

    // Checks for each item of the deserialized list.
    stack?.forEach { require(it.length in 1..32) }
  }

  // Override equals and hashCode for proper data comparison.

  /**
   * Compares this object to another for equality.
   *
   * @param other The other object to compare with.
   * @return `true` if the objects are equal, `false` otherwise.
   */
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

  /**
   * Generates a hash code for this object.
   *
   * @return The hash code value.
   */
  override fun hashCode(): Int {
    var result = nome?.hashCode() ?: 0
    result = 31 * result + (apelido?.hashCode() ?: 0)
    result = 31 * result + (nascimento?.hashCode() ?: 0)
    result = 31 * result + (stack?.contentHashCode() ?: 0)
    return result
  }
}

/**
 * Data class representing a person with a unique identifier and optional technology stack.
 *
 * @property id The unique identifier of the person.
 * @property nome The name of the person.
 * @property apelido The nickname of the person.
 * @property nascimento The date of birth of the person (in the format [yyyy-MM-dd]).
 * @property stack An array of technologies associated with the person.
 *
 * @throws IllegalArgumentException if any of the validation checks fail during object initialization.
 */
@Serializable
data class Pessoa(
  val id: UUID,
  val nome: String,
  val apelido: String,
  val nascimento: String,
  val stack: Array<String>?
) {
  // Override equals and hashCode for proper data comparison.

  /**
   * Compares this object to another for equality.
   *
   * @param other The other object to compare with.
   * @return `true` if the objects are equal, `false` otherwise.
   */
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

  /**
   * Generates a hash code for this object.
   *
   * @return The hash code value.
   */
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
 * Validates a date string to ensure it matches the fixed format: [yyyy-MM-dd].
 *
 * @param nascimento The date string to be validated.
 * @return `true` if the date string is valid, `false` otherwise.
 *
 * Note: The use of [StringBuilder] aims to minimize literal string allocations during validation.
 */
private fun validateNascimento(nascimento: String): Boolean {
  // Dates should always have a fixed size; if not, validation fails.
  if (nascimento.length != 10) return false

  val auxBuilder = StringBuilder()

  // Checks if "year" exists in the first 4 characters.
  if (
    auxBuilder
      .append(nascimento[0])
      .append(nascimento[1])
      .append(nascimento[2])
      .append(nascimento[3])
      .toString()
      .toIntOrNull() == null
  ) return false

  // Checks for the following separator.
  if (nascimento[4] != '-') return false

  auxBuilder.clear()

  // Checks if "month" exists in the following 2 characters.
  if (
    auxBuilder
      .append(nascimento[5])
      .append(nascimento[6])
      .toString()
      .toIntOrNull() == null
  ) return false

  // Checks for the second separator.
  if (nascimento[7] != '-') return false

  auxBuilder.clear()

  // Last check for the "day".
  return auxBuilder
    .append(nascimento[8])
    .append(nascimento[9])
    .toString()
    .toIntOrNull() != null
}