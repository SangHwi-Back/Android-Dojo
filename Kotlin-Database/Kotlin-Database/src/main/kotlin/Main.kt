package org.example

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.example.Users.Companion.keyColumn
import org.example.Users.Companion.nameColumn
import org.example.Users.Companion.birthColumn
import org.example.Users.Companion.emailColumn
import java.sql.Date
import kotlin.time.Duration.Companion.seconds

fun main() {
    val database = Database()
    val users = Users()
    database.insertTransaction(
        Transaction.InsertRows(users, TransactionElement.Row(
            listOf(
                users.newRow {
                    keyColumn set 0
                    nameColumn set "John"
                    birthColumn set Date.valueOf("2027-06-25")
                    emailColumn set "john@gmail.com"
                },
                users.newRow {
                    keyColumn set 1
                    nameColumn set "Macquarie"
                    birthColumn set Date.valueOf("2027-01-20")
                    emailColumn set "mac@gmail.com"
                },
                users.newRow {
                    keyColumn set 2
                    nameColumn set "Jane"
                    birthColumn set Date.valueOf("2027-02-28")
                    emailColumn set "jane@gmail.com"
                },
                users.newRow {
                    keyColumn set 3
                    nameColumn set "Rose"
                    birthColumn set Date.valueOf("2027-03-01")
                    emailColumn set "rose@gmail.com"
                },
            )
        ))
    )
    database.insertTransaction(
        Transaction.UpdateRecords(
            users,
            TransactionElement.Records(listOf(
                TableRecord(users.let { nameColumn }, "Colin")
            )),
            conditions = TransactionElement.Conditions(listOf(
                Where(users.let { keyColumn }, 3)
            ))
        )
    )

    println(users)
    runBlocking {
        delay(1.seconds)
        println(users)
    }
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