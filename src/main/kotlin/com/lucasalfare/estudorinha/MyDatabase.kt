package com.lucasalfare.estudorinha

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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
  suspend fun createPessoa(pessoaDTO: PessoaDTO): Result<UUID?> {
    val createdPessoaId = transaction(Database.connect(hikariDataSource)) {
      PessoasTable.insertIgnoreAndGetId {
        it[nome] = pessoaDTO.nome!!
        it[apelido] = pessoaDTO.apelido!!
        it[nascimento] = pessoaDTO.nascimento!!
        it[stack] = pessoaDTO.stack!!
      }?.value
    }

    return createdPessoaId?.let {
      dbQuery {
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
  suspend fun getPessoaById(id: UUID): Result<Pessoa?> {
    val search = dbQuery {
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
  suspend fun searchPessoasByTerm(term: String): Result<List<Pessoa>> {
    // TODO: check performance for the following "double SELECT"...
    val relatedPessoasIds = dbQuery {
      ConcatenationsTable.select {
        ConcatenationsTable.nomeApelidoStack like "%$term%"
      }.map { it[ConcatenationsTable.pessoaId] }
    }

    val relatedPessoas = dbQuery {
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
  suspend fun pessoasCount(): Result<String> {
    val countText = dbQuery {
      PessoasTable.selectAll().count().toString()
    }

    return Result(HttpStatusCode.OK, countText)
  }

  private suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(
      context = Dispatchers.IO,
      db = Database.connect(hikariDataSource)
    ) {
      block()
    }
}

/**
 * Creates and configures a HikariCP DataSource for connecting to a PostgreSQL database.
 *
 * @param jdbcUrl The JDBC URL of the PostgreSQL database.
 * @param username The username for authenticating the database connection.
 * @param password The password for authenticating the database connection.
 *
 * @return A configured HikariDataSource instance.
 */
private fun createHikariDataSource(
  jdbcUrl: String,
  username: String,
  password: String
): HikariDataSource {
  val hikariConfig = HikariConfig().apply {
    this.jdbcUrl = jdbcUrl
    // Always using PostgreSQL, so the driverClassName is fixed here
    this.driverClassName = "org.postgresql.Driver"
    this.username = username
    this.password = password
    this.maximumPoolSize = 20
    this.isAutoCommit = true
    this.transactionIsolation = IsolationLevel.TRANSACTION_READ_COMMITTED.name
    this.validate()
  }

  // Creating a new HikariDataSource instance using the configured HikariConfig
  return HikariDataSource(hikariConfig)
}

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