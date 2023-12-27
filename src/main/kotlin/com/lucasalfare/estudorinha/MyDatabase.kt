package com.lucasalfare.estudorinha

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * Singleton object representing the application's database.
 */
object MyDatabase {

  private lateinit var hikariDataSource: HikariDataSource

  /**
   * Initializes the database connection and creates tables if they don't exist.
   */
  fun initialize(
    address: String,
    databaseName: String,
    username: String,
    password: String
  ) {
    hikariDataSource = createHikariDataSource(
      jdbcUrl = "jdbc:postgresql://$address/$databaseName",
      username = username,
      password = password
    )

    //    transaction(Database.connect(hikariDataSource)) { SchemaUtils.drop(PessoasTable, ConcatenationsTable) } // tmp

    transaction(Database.connect(hikariDataSource)) {
      SchemaUtils.createMissingTablesAndColumns(PessoasTable, ConcatenationsTable)
    }
  }

  /**
   * Creates a new person in the database.
   *
   * @param pessoaDTO The data transfer object containing information about the person.
   * @return Result object with the HTTP status code and the ID of the created person.
   */
  fun createPessoa(pessoaDTO: PessoaDTO): Result<UUID?> {
    val createdPessoaId = transaction(Database.connect(hikariDataSource)) {
      PessoasTable.insertIgnoreAndGetId {
        it[nome] = pessoaDTO.nome!!
        it[apelido] = pessoaDTO.apelido!!
        it[nascimento] = pessoaDTO.nascimento!!
        it[stack] = pessoaDTO.stack!!
      }?.value
    }

    return createdPessoaId?.let {
      transaction(Database.connect(hikariDataSource)) {
        ConcatenationsTable.insert {
          it[nomeApelidoStack] = buildString {
            append(pessoaDTO.nome)
            append(pessoaDTO.apelido)
            pessoaDTO.stack?.forEach { item -> append(item) }
          }
          it[pessoaId] = createdPessoaId
        }
        Result(code = HttpStatusCode.Created, data = createdPessoaId)
      }
    } ?: Result(code = HttpStatusCode.UnprocessableEntity, null)
  }

  /**
   * Retrieves a person by their ID from the database.
   *
   * @param id The ID of the person to retrieve.
   * @return Result object with the HTTP status code and the retrieved person.
   */
  fun getPessoaById(id: UUID): Result<Pessoa?> {
    val search = transaction(Database.connect(hikariDataSource)) {
      PessoasTable.select {
        PessoasTable.id eq id
      }.singleOrNull()
    }?.toPessoa()

    return search?.let { Result(HttpStatusCode.OK, search) } ?: Result(HttpStatusCode.NotFound, null)
  }

  /**
   * Searches for people in the database based on a search term.
   *
   * @param term The search term.
   * @return Result object with the HTTP status code and a list of matching people.
   */
  fun searchPessoasByTerm(term: String): Result<List<Pessoa>> {
    // TODO: check performance for the following "double SELECT"...
    val relatedPessoasIds = transaction(Database.connect(hikariDataSource)) {
      ConcatenationsTable.select {
        ConcatenationsTable.nomeApelidoStack like "%$term%"
      }.map { it[ConcatenationsTable.pessoaId] }
    }

    val relatedPessoas = transaction {
      val searches = mutableListOf<Pessoa>()
      relatedPessoasIds.forEach {
        searches += PessoasTable.select { PessoasTable.id eq it }.single().toPessoa()
      }

      searches
    }

    return Result(code = HttpStatusCode.OK, data = relatedPessoas)
  }

  /**
   * Retrieves the count of people in the database.
   *
   * @return Result object with the HTTP status code and the count of people as a string.
   */
  fun pessoasCount(): Result<String> {
    val countText = transaction(Database.connect(hikariDataSource)) {
      PessoasTable.selectAll().count().toString()
    }

    return Result(HttpStatusCode.OK, countText)
  }
}

private fun createHikariDataSource(
  jdbcUrl: String,
  username: String,
  password: String
) = HikariDataSource(HikariConfig().apply {
  this.jdbcUrl = jdbcUrl
  this.driverClassName = "org.postgresql.Driver" // always using postgres, then is fixed here
  this.username = username
  this.password = password
  this.maximumPoolSize = 3
  this.isAutoCommit = false
  this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
  this.validate()
})

/**
 * Extension function to convert a [ResultRow] to a [Pessoa] object.
 */
fun ResultRow.toPessoa(): Pessoa {
  return Pessoa(
    id = this[PessoasTable.id].value,
    nome = this[PessoasTable.nome],
    apelido = this[PessoasTable.apelido],
    nascimento = this[PessoasTable.nascimento],
    stack = this[PessoasTable.stack]
  )
}