package com.lucasalfare

import io.ktor.http.*
import java.util.*

object Database {

  private val tmpData = mutableListOf<Pessoa>()

  fun createPessoa(pessoaDTO: PessoaDTO): Result<UUID?> {
    // TODO: validates when "apelido" is not unique in DB (return same above)

    if (tmpData.any { it.apelido == pessoaDTO.apelido }) {
      return Result(code = HttpStatusCode.UnprocessableEntity, null)
    }

    val createdPessoa = Pessoa(
      id = UUID.randomUUID(),
      nome = pessoaDTO.nome!!,
      apelido = pessoaDTO.apelido!!,
      nascimento = pessoaDTO.nascimento!!,
      stack = pessoaDTO.stack
    )

    // TODO: inserts "createdPessoa" into DB

    tmpData += createdPessoa

    return Result(code = HttpStatusCode.Created, createdPessoa.id)
  }

  fun getPessoaById(id: UUID): Result<Pessoa?> {
    // TODO: check if DB contains some "Pessoa" with [id] value

    val search = tmpData.find { it.id == id }
    if (search == null) return Result(HttpStatusCode.NotFound, null)
    return Result(HttpStatusCode.OK, search)
  }
}