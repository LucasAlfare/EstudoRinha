package com.lucasalfare

import io.ktor.http.*

/**
 * A generic data class representing the result of an operation or API response.
 * Also, [TData] should represent the type of data contained in the result instance.
 *
 * @property code The HTTP status code indicating the outcome of the operation.
 * @property data The data associated with the result, if applicable.
 *
 * Note: This class is typically used to encapsulate the outcome of operations or API responses,
 * providing both the HTTP status code and any associated data.
 */
data class Result<TData>(
  val code: HttpStatusCode,
  val data: TData
)