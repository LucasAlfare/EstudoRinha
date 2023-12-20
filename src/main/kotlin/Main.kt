package com.lucasalfare

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun main() {
  Database.connect(
    url = "jdbc:postgresql://localhost:5433/",
    driver = "org.postgresql.Driver",
    user = "postgres",
    password = "5411459"
  )

  transaction { SchemaUtils.drop(PessoasTable) } // tmp

  embeddedServer(
    Netty,
    port = 8080,
    host = "0.0.0.0",
    module = Application::myApp
  ).start(wait = true)
}

fun Application.myApp() {
  install(ContentNegotiation) {
    json()
  }

  configureRouting()
}

fun Application.configureRouting() {
  routing {
    /*
    exemplo de request para teste:

    curl -v -d '{"apelido": "lucas", "nome": "francisco lucas", "nascimento": "9999-99-99", "stack": ["kotlin"] }' -H 'Content-Type: application/json' http://127.0.0.1:8080/pessoas
     */
    post("/pessoas") {
      runCatching {
        // using directly [kotlinx.serialization.json.Json] to avoid wrong parsing of req stacks array
        val pessoaDTO = Json.decodeFromString<PessoaDTO>(call.receiveText())
        val result = MyDatabase.createPessoa(pessoaDTO)

        call.response.headers.append(
          name = HttpHeaders.Location,
          value = "/pessoas/${result.data}"
        )

        call.respond(result.code)
      }.onFailure {
        call.respond(HttpStatusCode.BadRequest)
      }
    }

    /*
    exemplo de request para teste:

    curl -v http://127.0.0.1:8080/pessoas/UUID
     */
    get("/pessoas/{id}") {
      val requestId = call.parameters["id"]!!
      val result = MyDatabase.getPessoaById(UUID.fromString(requestId))
      if (result.data == null) {
        call.respond(result.code)
      } else {
        call.respond(result.code, result.data)
      }
    }
  }
}

//01955196-2f8e-4678-8d42-859b309ec8f8