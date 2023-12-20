package com.lucasalfare

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.TextColumnType

object PessoasTable : UUIDTable("Pessoas") {

  val nome = varchar("nome", 100)
  val apelido = varchar("apelido", 32).uniqueIndex()
  val nascimento = varchar("nascimento", 10)
  val stack = array<String>("stack", TextColumnType())
}