package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.example.TableColumn
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

val database = Database()

suspend fun main() {
    val path = "data/schema"

    if (File(path).exists().not()) return

    val files = File(path).listFiles()!!

    println(files.contentToString())

    val json = Json { ignoreUnknownKeys = true }

    for (file in files) {
        val jsonString = file.readText()
        val rawSchema: RawSchema = json.decodeFromString<RawSchema>(jsonString)

        val columns = rawSchema.columns.map {
            TableColumn(it.name, TableColumnType.string(it.type))
        }

        // JsonObject를 TableRow로 변환
        val rows = rawSchema.rows.mapNotNull { jsonObject ->
            val records = mutableListOf<TableRecord<Any>>()

            for ((key, jsonValue) in jsonObject) {
                val column = columns.find { it.name == key }
                val value = (jsonValue as? JsonPrimitive)?.content?.let {
                    column?.type?.validate(it)
                }

                if (column != null && value != null) {
                    records.add(TableRecord(column, value))
                }
            }

            if (records.size != columns.size) return@mapNotNull null

            TableRow(records)

        }.toMutableList()

        val table = object : Table(rawSchema.tableName) {
            override val tableColumns: List<TableColumn<Any>> = columns
            override var tableRows: MutableList<TableRow> = rows
        }

        database.tables[rawSchema.tableName] = table
    }

    CoroutineScope(Dispatchers.IO).launch {
        database.selectTransactionFlow.collect {
            println("Select 결과 ::: $it")
        }
    }

    CoroutineScope(Dispatchers.IO).launch {
        database.transactionEffectFlow.collect {
            println("Throw 결과 ::: $it")
        }
    }

    database.tables["Users"]?.let { table ->
        Transaction(table.hashCode().toString(), listOf(
            Operation.Insert(table.name, table.newRow {
                TableColumn("name", TableColumnType.Varchar) set "BigFoot"
                TableColumn("birth", TableColumnType.DateTime) set "1990-03-31"
                TableColumn("email", TableColumnType.Varchar) set "bigfoot@pentagon.com"
            }),
            Operation.Select(table.name, table.tableColumns),
            Operation.Insert(table.name, table.newRow {
                TableColumn("name", TableColumnType.Varchar) set "TestUser"
                TableColumn("birth", TableColumnType.DateTime) set "2000-01-01"
                TableColumn("email", TableColumnType.Varchar) set "testuser@example.com"
            })
        )).let {
            database.insertTransaction(it)
        }

        Transaction(table.hashCode().toString(), listOf(
            Operation.Insert(table.name, table.newRow {
                TableColumn("key", TableColumnType.NumberInt) set 0
                TableColumn("name", TableColumnType.Varchar) set "ReleaseGhost"
                TableColumn("birth", TableColumnType.DateTime) set "2000-03-31"
                TableColumn("email", TableColumnType.Varchar) set "ghost@pentagon.com"
            }),
        )).let {
            database.insertTransaction(it)
        }

        Transaction(table.hashCode().toString(), listOf(
            Operation.Update(
                table.name,
                listOf(
                    TableRecord(TableColumn("name", TableColumnType.Varchar), "UpdatedUser"),
                    TableRecord(TableColumn("birth", TableColumnType.DateTime), "2001-01-01"),
                    TableRecord(TableColumn("email", TableColumnType.Varchar), "updateduser@example.com")
                ),
                listOf()
            ),
            Operation.Delete(table.name, ConditionDelete.ID(0))
        )).let {
            database.insertTransaction(it)
        }
    }

    delay(1000L.milliseconds)

    for ((key, table) in database.tables) {
        println("[[[ $key ]]]\n$table")
    }
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
        return if (it >= 0) tableRecords[it] else null
    }
