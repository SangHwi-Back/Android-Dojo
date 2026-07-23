package org.example

import java.sql.Date

data class TableColumn(
    val name: String,
    val dataType: DBDataType,
) {
    companion object {
        val Key = TableColumn("Key", dataType = DBDataType.NUMBER)
    }
}

sealed class TableColumnType<T> {
    abstract fun validate(value: Any?): T

    object NumberInt: TableColumnType<Int>() {
        override fun validate(value: Any?): Int =
            value as? Int ?: throw IllegalArgumentException()
    }
    object NumberDouble: TableColumnType<Double>() {
        override fun validate(value: Any?): Double =
            value as? Double ?: throw IllegalArgumentException()
    }
    object Varchar: TableColumnType<String>() {
        override fun validate(value: Any?): String =
            value as? String ?: throw IllegalArgumentException()
    }
    object DateTime: TableColumnType<Date>() {
        override fun validate(value: Any?): Date =
            value as? Date ?: throw IllegalArgumentException()
    }
}

data class TableRow(
    val tableRecords: MutableList<TableRecord>
) {
    override fun toString(): String =
        tableRecords.joinToString(", ")
    companion object {
        fun increment(key: String, tableRecords: List<TableRecord>): TableRow =
            TableRow(mutableListOf(
                TableRecord(TableColumn.Key, key)
            ).apply {
                addAll(tableRecords)
            })
    }
}

data class TableRecord(
    val tableColumn: TableColumn,
    var data: String,
) {
    override fun toString(): String =
        "[{${tableColumn.dataType}} $tableColumn]: $data"
}
interface TableColumns {
    val tableColumns: List<TableColumn>
}

interface TableRows {
    var tableRows: MutableList<TableRow>
}

abstract class Table(val name: String): TableColumns, TableRows {
    override fun toString(): String {
        val colWidths = tableColumns.map { col ->
            maxOf(
                col.name.length,
                tableRows.maxOfOrNull { row ->
                    row.tableRecords.firstOrNull { it.tableColumn == col }?.data?.length ?: 0
                } ?: 0
            )
        }

        fun separator() = "+" + colWidths.joinToString("+") { "-".repeat(it + 2) } + "+"

        fun row(values: List<String>) =
            "|" + values.mapIndexed { i, v -> " ${v.padEnd(colWidths[i])} " }.joinToString("|") + "|"

        return buildString {
            appendLine(separator())
            appendLine(row(tableColumns.map { it.name }))
            appendLine(separator())
            for (tableRow in tableRows) {
                val values = tableColumns.map { col ->
                    tableRow.tableRecords.firstOrNull { it.tableColumn == col }?.data ?: ""
                }
                appendLine(row(values))
            }
            append(separator())
        }
    }
    fun increment(tableRecords: List<TableRecord>) =
        tableRows
            .mapNotNull { row ->
                row.getRecord("key")?.data?.toInt()
            }
            .maxOrNull()?.let { key ->
                tableRows.add(TableRow.increment("${key+1}", tableRecords))
            }
    fun increment(row: TableRow) = increment(row.tableRecords)
}
class RowBuilder(private val columns: List<TableColumn>) {
    private val _values = mutableMapOf<String, String>()
    val values: Map<String, String>
        get() = _values.toMap()

    infix fun String.set(value: String) {
        _values[this] = value
    }

    fun build(): TableRow {
        val records = columns.map { col ->
            TableRecord(col, _values[col.name] ?: "")
        }
        return TableRow(records.toMutableList())
    }
}
@Throws(IllegalArgumentException::class)
inline fun Table.newRow(block: RowBuilder.() -> Unit): TableRow {
    val builder = RowBuilder(tableColumns)
    builder.block()
    if (builder.values.size != tableColumns.size) {
        throw IllegalArgumentException("[RowBuilder.build]-[Table.newRow] New Row should have exactly size (expect: ${tableColumns.size}, actual: ${builder.values.size})")
    }
    return builder.build()
}