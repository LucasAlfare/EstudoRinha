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

/**
 * Main function to start the web server and define API routes.
 */
fun main() {
  // Initializes the database before starting the web server.
  MyDatabase.initialize()

  // Starts the embedded web server using Netty.
  embeddedServer(
    Netty,
    port = 8080,
    host = "0.0.0.0",
    module = {
      // Installs Content Negotiation with a non-lenient JSON parser.
      install(ContentNegotiation) {
        json(
          json = Json {
            isLenient = false
          }
        )
      }

      // Defines API routes.
      routing {
        /*
        Example request for testing:

        curl -v -d '{"apelido": "lucas", "nome": "francisco lucas", "nascimento": "9999-99-99", "stack": ["kotlin", 1] }' -H 'Content-Type: application/json' http://127.0.0.1:8080/pessoas
         */
        post("/pessoas") {
          runCatching {
            // Receives and processes the PessoaDTO from the request.
            val pessoaDTO = call.receive<PessoaDTO>()
            // Creates a new person in the database.
            val result = MyDatabase.createPessoa(pessoaDTO)

            // Sets the Location header with the URL of the newly created resource.
            call.response.headers.append(
              name = HttpHeaders.Location,
              value = "/pessoas/${result.data}"
            )

            // Responds with the HTTP status code.
            call.respond(result.code)
          }.onFailure {
            // Responds with a BadRequest status code and error message in case of failure.
            call.respond(HttpStatusCode.BadRequest, it.message ?: "error")
          }
        }

        /*
        Example request for testing:

        curl -v http://127.0.0.1:8080/pessoas/UUID
        auxUUID = 01955196-2f8e-4678-8d42-859b309ec8f8
         */
        get("/pessoas/{id}") {
          // Retrieves the person by ID from the database.
          val requestId = call.parameters["id"]!!
          val result = MyDatabase.getPessoaById(UUID.fromString(requestId))

          // Responds with the HTTP status code and retrieved person data if available.
          if (result.data == null) {
            call.respond(result.code)
          } else {
            call.respond(result.code, result.data)
          }
        }

        /*
        Example request for testing:

        curl -v http://127.0.0.1:8080/pessoas/t=term
         */
        get("/pessoas/t={termo}") {
          runCatching {
            // Retrieves people based on a search term from the database.
            val term = call.parameters["termo"]!!
            if (term.isEmpty()) return@get call.respond(HttpStatusCode.BadRequest)

            val result = MyDatabase.searchPessoasByTerm(term)
            call.respond(HttpStatusCode.OK, result.data)
          }.onFailure {
            // Responds with a BadRequest status code and error message in case of failure.
            call.respond(HttpStatusCode.BadRequest, it.message ?: "error")
          }
        }

        /*
        Example request for testing:

        curl -v http://127.0.0.1:8080/contagem-pessoas
         */
        get("/contagem-pessoas") {
          // Retrieves the count of people from the database.
          val result = MyDatabase.pessoasCount()
          // Responds with the HTTP status code and count data.
          call.respond(result.code, result.data)
        }
      }
    }
  ).start(wait = true)
}