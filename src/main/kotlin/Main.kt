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
        // now JSON parser is not "lenient" about weird things, such as
        // parsing to string a malformed JSON array.
        // (e.g.: '["string1", "str", 32]' -> the number 32 is not turned into string.
        json(
          json = Json {
            isLenient = false
          }
        )
      }

      // Routes definitions here
      routing {
        /*
        exemplo de request para teste:

        curl -v -d '{"apelido": "lucas", "nome": "francisco lucas", "nascimento": "9999-99-99", "stack": ["kotlin", 1] }' -H 'Content-Type: application/json' http://127.0.0.1:8080/pessoas
         */
        post("/pessoas") {
          runCatching {
            val pessoaDTO = call.receive<PessoaDTO>()
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