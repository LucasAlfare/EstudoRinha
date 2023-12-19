package com.lucasalfare

data class Result<TCode, TResult>(
  val code: TCode,
  val data: TResult
)