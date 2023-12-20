package com.lucasalfare.estudorinha

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType

/**
 * Definition of the "Pessoas" table using UUIDTable to store information about individuals.
 */
object PessoasTable : UUIDTable("Pessoas") {

  /**
   * Column to store the person's name with a maximum of 100 characters.
   */
  val nome = varchar("nome", 100)

  /**
   * Column to store the person's nickname with a maximum of 32 characters, ensuring uniqueness.
   */
  val apelido = varchar("apelido", 32).uniqueIndex()

  /**
   * Column to store the person's date of birth with a maximum of 10 characters.
   */
  val nascimento = varchar("nascimento", 10)

  /**
   * Column to store a list of technologies ("stack") associated with the person.
   */
  val stack = array<String>("stack", TextColumnType())
}

/**
 * Definition of the "Concatenations" table to store concatenations of people's information.
 */
object ConcatenationsTable : Table("Concatenations") {

  /**
   * Column to store the concatenation of the person's name, nickname, and stack.
   */
  val nomeApelidoStack = text("nome_apelido_stack")

  /**
   * Column to store the ID of the person associated with this concatenation, referencing the "Pessoas" table.
   */
  val pessoaId = uuid("pessoa_id").references(PessoasTable.id)
}