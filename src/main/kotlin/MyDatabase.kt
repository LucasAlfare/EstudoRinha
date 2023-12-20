package com.lucasalfare

import io.ktor.http.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object MyDatabase {

  init {
    Database.connect(
      url = "jdbc:postgresql://localhost:5433/",
      driver = "org.postgresql.Driver",
      user = "postgres",
      password = "5411459"
    )

    transaction { SchemaUtils.drop(PessoasTable, ConcatenationsTable) } // tmp

    transaction {
      SchemaUtils.createMissingTablesAndColumns(PessoasTable, ConcatenationsTable)
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

    if (createdPessoaId != null) {
      transaction {
        ConcatenationsTable.insert {
          it[nomeApelidoStack] = buildString {
            append(pessoaDTO.nome)
            append(pessoaDTO.apelido)
            pessoaDTO.stack?.forEach { item -> append(item) }
          }
          it[pessoaId] = createdPessoaId
        }
      }

      return Result(code = HttpStatusCode.Created, data = createdPessoaId)
    }

    return Result(code = HttpStatusCode.UnprocessableEntity, null)
  }

  fun getPessoaById(id: UUID): Result<Pessoa?> {
    val search = transaction {
      PessoasTable.select {
        PessoasTable.id eq id
      }.singleOrNull()
    }?.toPessoa()

    if (search == null) return Result(HttpStatusCode.NotFound, null)
    return Result(HttpStatusCode.OK, search)
  }

  fun searchPessoasByTerm(term: String): Result<MutableSet<Pessoa>> {
    // TODO: check performance for the following "double SELECT"...
    val relatedPessoaId = transaction {
      ConcatenationsTable.select {
        ConcatenationsTable.nomeApelidoStack like "%$term%"
      }.map { it[ConcatenationsTable.pessoaId] }
    }

    val relatedPessoas = transaction {
      val searches = mutableSetOf<Pessoa>()
      relatedPessoaId.forEach {
        searches += PessoasTable.select { PessoasTable.id eq it }.single().toPessoa()
      }

      searches
    }

    return Result(code = HttpStatusCode.OK, data = relatedPessoas)
  }

  fun pessoasCount(): Result<String> {
    val countText = transaction {
      PessoasTable.selectAll().count().toString()
    }

    return Result(HttpStatusCode.OK, countText)
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