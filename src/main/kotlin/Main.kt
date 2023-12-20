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
    exemplo de request para teste:

    curl -v -d '{"apelido": "lucas", "nome": "francisco lucas", "nascimento": "1994-10-18", "stack": ["kotlin"] }' -H 'Content-Type: application/json' http://127.0.0.1:8080/pessoas
     */
    post("/pessoas") {
      runCatching {
//        val pessoaDTO = call.receive<PessoaDTO>() // WHY THIS parses "correctly" the case of JSON '{"apelido": "nicknick", "nome": "Nick Nickian", "nascimento": "1994-10-18", "stack": [1, "java", "kotlin"] }'
        val pessoaDTO = Json.decodeFromString<PessoaDTO>(call.receiveText())
        val result = Database.createPessoa(pessoaDTO)
        call.response.headers.append(name = HttpHeaders.Location, value = "/pessoas/${result.data}")
        call.respond(result.code)
      }.onFailure {
        call.respond(
          HttpStatusCode.BadRequest,
//          message = it.message ?: "ok!"
        )
      }
    }

    /*
    exemplo de request para teste:

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
