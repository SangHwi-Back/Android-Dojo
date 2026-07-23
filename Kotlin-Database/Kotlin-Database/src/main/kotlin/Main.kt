package org.example

import org.example.Users.Companion.keyColumn
import org.example.Users.Companion.nameColumn
import org.example.Users.Companion.birthColumn
import org.example.Users.Companion.emailColumn
import java.sql.Date

fun main() {
    val users = Users().apply {
        try {
            insertRow(newRow {
                keyColumn set 3
                nameColumn set "John"
                birthColumn set Date(2027, 7, 23)
                emailColumn set "john@gmail.com"
            })
        } catch (exception: IllegalArgumentException) {
            println("계획대로 $exception")
        }
        updateRecords(
            tableRecord = TableRecord(nameColumn, "Colin"),
            where = listOf(Where(keyColumn, 3))
        )
    }
    println(users)
    println(EnvironmentTable())
}

fun MutableList<TableRow>.getRowIndex(key: String): Int? {
    for ((index, row) in this.withIndex())
        row.getRecord<Int>(TableColumn.Key)?.let {
            if (it.data == key.toInt()) return index
        }
    return null
}
fun MutableList<out TableRecord<Any>>.columnIndex(name: String): Int =
    map { it.tableColumn.name }.indexOf(name)
fun MutableList<out TableRecord<Any>>.columnIndex(column: TableColumn<Any>): Int =
    map { it.tableColumn }.indexOf(column)
fun <T> TableRow.getRecord(columnName: String): TableRecord<T>? =
    tableRecords.columnIndex(columnName).let {
        return if (it >= 0) tableRecords[it] as? TableRecord<T> else null }
fun <T> TableRow.getRecord(column: TableColumn<Any>): TableRecord<T>? =
    tableRecords.columnIndex(column).let {
        return if (it >= 0) tableRecords[it] as? TableRecord<T> else null }