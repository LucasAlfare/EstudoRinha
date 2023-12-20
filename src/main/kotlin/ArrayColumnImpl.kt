/**
 * @author MrPowerGammerBr
 */
package com.lucasalfare

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager

/**
 * Extension function for a `Table` to define an array column.
 *
 * @param name The name of the array column.
 * @param columnType The column type of the array elements.
 * @return The defined array column.
 */
fun <T> Table.array(name: String, columnType: ColumnType): Column<Array<T>> =
  registerColumn(name, ArrayColumnType(columnType))

/**
 * Custom `ColumnType` implementation to support array columns.
 *
 * @param type The column type of the array elements.
 */
class ArrayColumnType(private val type: ColumnType) : ColumnType() {

  /**
   * Checks if the database supports arrays.
   *
   * @return `true` if arrays are supported, `false` otherwise.
   */
  private fun supportsArrays() = true

  /**
   * Gets the SQL type representation for the array column.
   *
   * @return The SQL type representation.
   */
  override fun sqlType(): String = buildString {
    if (!supportsArrays()) {
      append("TEXT")
    } else {
      append(type.sqlType())
      append(" ARRAY")
    }
  }

  /**
   * Converts the value to a format suitable for database storage.
   *
   * @param value The value to be converted.
   * @return The converted value.
   */
  override fun valueToDB(value: Any?): Any? {
    if (!supportsArrays())
      return "'NOT SUPPORTED'"

    if (value is Array<*>) {
      val columnType = type.sqlType().split("(")[0]
      val jdbcConnection = (TransactionManager.current().connection as JdbcConnectionImpl).connection
      return jdbcConnection.createArrayOf(columnType, value)
    } else {
      return super.valueToDB(value)
    }
  }

  /**
   * Converts the value retrieved from the database to the corresponding Kotlin type.
   *
   * @param value The value retrieved from the database.
   * @return The converted Kotlin type value.
   */
  override fun valueFromDB(value: Any): Any {
    if (!supportsArrays()) {
      val clazz = type::class
      val clazzName = clazz.simpleName
      if (clazzName == "LongColumnType")
        return arrayOf<Long>()
      if (clazzName == "TextColumnType")
        return arrayOf<String>()
      error("Unsupported Column Type")
    }

    if (value is java.sql.Array) {
      return value.array
    }
    if (value is Array<*>) {
      return value
    }
    error("Array is not supported for this database")
  }

  /**
   * Converts the non-null value to a format suitable for database storage.
   *
   * @param value The non-null value to be converted.
   * @return The converted value.
   */
  override fun notNullValueToDB(value: Any): Any {
    if (!supportsArrays())
      return "'NOT SUPPORTED'"

    if (value is Array<*>) {
      if (value.isEmpty())
        return "'{}'"

      val columnType = type.sqlType().split("(")[0]
      val jdbcConnection = (TransactionManager.current().connection as JdbcConnectionImpl).connection
      return jdbcConnection.createArrayOf(columnType, value) ?: error("Can't create non-null array for $value")
    } else {
      return super.notNullValueToDB(value)
    }
  }
}