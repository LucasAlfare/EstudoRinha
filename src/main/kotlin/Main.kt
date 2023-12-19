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
import kotlinx.serialization.SerializationException
import java.util.*

fun main() {
  embeddedServer(
    Netty,
    port = 8080,
    host = "0.0.0.0",
    module = Application::myModule
  ).start(wait = true)
}

fun Application.myModule() {
  install(ContentNegotiation) {
    json()
  }

  configureRouting()
}

fun Application.configureRouting() {
  routing {
    /*
    curl -v -d '{"apelido": 1, "nome": "francisco lucas", "nascimento": "1994-10-18", "stack": ["kotlin"] }' -H 'Content-Type: application/json' http://127.0.0.1:8080/pessoas
     */
    post("/pessoas") {
      try {
        val pessoaDTO = call.receive<PessoaDTO>()
        val result = Database.createPessoa(pessoaDTO)
        call.response.headers.append(name = HttpHeaders.Location, value = "/pessoas/${result.data}")
        call.respond(result.code)
      } catch (e: SerializationException) {
        call.respond(HttpStatusCode.BadRequest)
      }
    }

    /*
    curl -v http://127.0.0.1:8080/pessoas/{UUID}
     */
    get("/pessoas/{id}") {
      val requestId = call.parameters["id"]!!
      val result = Database.getPessoaById(UUID.fromString(requestId))
      if (result.data == null) {
        call.respond(result.code)
      } else {
        call.respond(result.code, result.data)
      }
    }
  }
}
