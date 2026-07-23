package org.example

import org.example.Users.Companion.keyColumn
import org.example.Users.Companion.nameColumn

fun main() {
    val users = Users().apply {
        try {
            newRow {
                "Test" set "value_test"
                "Testing" set "value_test"
            }
        } catch (exception: IllegalArgumentException) {
            println("계획대로 $exception")
        }
        updateRecords(
            tableRecord = TableRecord(nameColumn, "Colin"),
            where = listOf(Where(keyColumn, "3"))
        )
        try {
            deleteRow("4")
        } catch (exception: IllegalArgumentException) {
            println("계획대로 $exception")
        }
    }
    println(users)
    println(EnvironmentTable())
}

fun MutableList<TableRow>.getRowIndex(key: String): Int? {
    for ((index, row) in this.withIndex())
        row.getRecord(TableColumn.Key)?.let {
            if (it.data == key) return index
        }
    return null
}
fun MutableList<TableRecord>.columnIndex(name: String): Int =
    map { it.tableColumn.name }.indexOf(name)
fun MutableList<TableRecord>.columnIndex(column: TableColumn): Int =
    map { it.tableColumn }.indexOf(column)
fun TableRow.getRecord(columnName: String): TableRecord? =
    tableRecords.columnIndex(columnName).let { return if (it >= 0) tableRecords[it] else null }
fun TableRow.getRecord(column: TableColumn): TableRecord? =
    tableRecords.columnIndex(column).let { return if (it >= 0) tableRecords[it] else null }

enum class DBDataType {
    NUMBER,
    VARCHAR,
    DATE,
    EMAIL;
    // Interfaces
    override fun toString(): String = when (this) {
        NUMBER -> "Number"
        VARCHAR -> "Varchar"
        DATE -> "Date"
        EMAIL -> "Email"
    }
    fun checkType(data: String): Boolean = when (this) {
        NUMBER -> data.toIntOrNull() != null || data.toDoubleOrNull() != null
        VARCHAR -> true
        DATE -> data.split("-").let {
            if (it.size < 3)
                return false
            for (num in it)
                if (num.toIntOrNull() == null)
                    return false
            return true
        }
        EMAIL -> data.split("@").let { split ->
            if (split.size < 2)
                return false
            split[1].split(".").let { split ->
                return split.size >= 2
            }
        }
    }
}
// KMP 에서는 Lambda-Receiver DSL,