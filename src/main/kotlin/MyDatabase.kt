package com.lucasalfare

import io.ktor.http.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object MyDatabase {

  init {
    transaction {
      SchemaUtils.createMissingTablesAndColumns(PessoasTable)
    }
  }

  fun createPessoa(pessoaDTO: PessoaDTO): Result<UUID?> {
    val createdPessoaId = transaction {
      PessoasTable.insertIgnoreAndGetId {
        it[nome] = pessoaDTO.nome!!
        it[apelido] = pessoaDTO.apelido!!
        it[nascimento] = pessoaDTO.nascimento!!
        it[stack] = pessoaDTO.stack!!
      }?.value
    }

    return Result(
      code = if (createdPessoaId == null) HttpStatusCode.UnprocessableEntity else HttpStatusCode.Created,
      data = createdPessoaId
    )
  }

  fun getPessoaById(id: UUID): Result<Pessoa?> {
    // TODO: check if DB contains some "Pessoa" with [id] value

    val search = transaction {
      PessoasTable.select {
        PessoasTable.id eq id
      }.singleOrNull()
    }?.toPessoa()

    if (search == null) return Result(HttpStatusCode.NotFound, null)
    return Result(HttpStatusCode.OK, search)
  }
}

fun ResultRow.toPessoa(): Pessoa {
  return Pessoa(
    id = this[PessoasTable.id].value,
    nome = this[PessoasTable.nome],
    apelido = this[PessoasTable.apelido],
    nascimento = this[PessoasTable.nascimento],
    stack = this[PessoasTable.stack]
  )
}