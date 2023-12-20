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
import java.util.*

fun main() {
  // initializes database before webserver? (o.o)
  MyDatabase.initialize()

  embeddedServer(
    Netty,
    port = 8080,
    host = "0.0.0.0",
    module = {
      install(ContentNegotiation) {
        // TODO: update "isLenient" configuration
        json()
      }

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
            call.respond(HttpStatusCode.BadRequest, it.message ?: "error")
          }
        }

        /*
        exemplo de request para teste:

        curl -v http://127.0.0.1:8080/pessoas/UUID
        auxUUID = 01955196-2f8e-4678-8d42-859b309ec8f8
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

        /*
        curl -v http://127.0.0.1:8080/pessoas/t=
         */
        get("/pessoas/t={termo}") {
          runCatching {
            val term = call.parameters["termo"]!!
            if (term.isEmpty()) return@get call.respond(HttpStatusCode.BadRequest)

            val result = MyDatabase.searchPessoasByTerm(term)
            call.respond(HttpStatusCode.OK, result.data)
          }.onFailure {
            call.respond(HttpStatusCode.BadRequest, it.message ?: "error")
          }
        }

        /*
        curl -v http://127.0.0.1:8080/contagem-pessoas
         */
        get("/contagem-pessoas") {
          val result = MyDatabase.pessoasCount()
          call.respond(result.code, result.data)
        }
      }
    }
  ).start(wait = true)
}