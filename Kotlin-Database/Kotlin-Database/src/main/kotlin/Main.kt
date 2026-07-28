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