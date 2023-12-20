package com.lucasalfare

import io.ktor.http.*

data class Result<TResult>(
  val code: HttpStatusCode,
  val data: TResult
)