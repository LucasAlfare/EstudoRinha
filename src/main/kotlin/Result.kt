package com.lucasalfare

import io.ktor.http.*

data class Result<TData>(
  val code: HttpStatusCode,
  val data: TData
)