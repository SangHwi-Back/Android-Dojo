package org.example

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import java.io.File

fun main() {
    val path = "data/schema"

    if (File(path).exists().not()) return

    val files = File(path).listFiles()!!

    println(files.contentToString())

    val json = Json { ignoreUnknownKeys = true }

    for (file in files) {
        val jsonString = file.readText()
        val rawSchema: RawSchema = json.decodeFromString<RawSchema>(jsonString)

        // JsonObject를 TableRow로 변환
        val rows = rawSchema.rows.map { jsonObject ->
            val records = mutableListOf<TableRecord<Any>>()

            for ((key, jsonValue) in jsonObject) {
                val column = rawSchema.columns.find { it.name == key }
                if (column != null) {
                    val value = (jsonValue as? JsonPrimitive)?.content ?: jsonValue.toString()
                    val tableColumn = TableColumn(column.name, TableColumnType.string(column.type))
                    records.add(TableRecord(tableColumn, value))
                }
            }

            TableRow(records)
        }.toMutableList()

        val table = object : Table(rawSchema.tableName) {
            override val tableColumns: List<TableColumn<Any>>
                get() = rawSchema.columns.map {
                    TableColumn(it.name, TableColumnType.string(it.type))
                }
            override var tableRows: MutableList<TableRow> = rows
        }

        println(table)
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
        return if (it >= 0) tableRecords[it] else null }