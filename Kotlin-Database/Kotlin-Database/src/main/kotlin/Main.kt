package org.example

import org.example.Users.Companion.keyColumn
import org.example.Users.Companion.nameColumn
import org.example.Users.Companion.birthColumn
import org.example.Users.Companion.emailColumn
import java.sql.Date

fun main() {
    val users = Users().apply {
        try {
            insertRow(
                newRow {
                    keyColumn set 0
                    nameColumn set "John"
                    birthColumn set Date.valueOf("2027-06-25")
                    emailColumn set "john@gmail.com"
                },
                newRow {
                    keyColumn set 1
                    nameColumn set "Macquarie"
                    birthColumn set Date.valueOf("2027-01-20")
                    emailColumn set "mac@gmail.com"
                },
                newRow {
                    keyColumn set 2
                    nameColumn set "Jane"
                    birthColumn set Date.valueOf("2027-02-28")
                    emailColumn set "jane@gmail.com"
                },
                newRow {
                    keyColumn set 3
                    nameColumn set "Rose"
                    birthColumn set Date.valueOf("2027-03-01")
                    emailColumn set "rose@gmail.com"
                },
                newRow {
                    // Intended missing key row
                    nameColumn set "BigFoot"
                    birthColumn set Date.valueOf("1990-03-31")
                    emailColumn set "bigfoot@pentagon.com"
                }
            )
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
        row.getRecord(TableColumn.Key)?.let {
            val keyFromData = it.data as? Int
            if (keyFromData != null && keyFromData == key.toInt()) return index
        }
    return null
}
fun MutableList<out TableRecord<Any>>.columnIndex(column: TableColumn<Any>): Int =
    map { it.tableColumn }.indexOf(column)
fun TableRow.getRecord(column: TableColumn<Any>): TableRecord<Any>? =
    tableRecords.columnIndex(column).let {
        return if (it >= 0) tableRecords[it] else null }