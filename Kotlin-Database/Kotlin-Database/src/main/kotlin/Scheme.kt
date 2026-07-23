package org.example

import java.sql.Date

data class TableColumn<out T>(val name: String, val type: TableColumnType<out T>) {
    override fun equals(other: Any?): Boolean {
        val other = other as? TableColumn<*>
        if (other != null) {
            return other.name == name && other.type::class == type::class
        }
        return false
    }

    companion object {
        val Key = TableColumn("Key", TableColumnType.NumberInt)
    }
}

sealed class TableColumnType<T> {
    abstract fun validate(value: Any?): T

    object NumberInt: TableColumnType<Int>() {
        override fun validate(value: Any?): Int =
            value as? Int ?: throw IllegalArgumentException("Value must be a integer $value.")
    }
    object NumberDouble: TableColumnType<Double>() {
        override fun validate(value: Any?): Double =
            value as? Double ?: throw IllegalArgumentException("Value must be a float or double $value.")
    }
    object Varchar: TableColumnType<String>() {
        override fun validate(value: Any?): String =
            value as? String ?: throw IllegalArgumentException("Value is not a string $value.")
    }
    object DateTime: TableColumnType<Date>() {
        override fun validate(value: Any?): Date =
            value as? Date ?: throw IllegalArgumentException("Date cannot be converted $value.")
    }
}

data class TableRow(
    val tableRecords: MutableList<TableRecord<Any>>
) {
    override fun toString(): String =
        tableRecords.joinToString(", ")
    companion object {
        fun increment(key: String, tableRecords: List<TableRecord<Any>>): TableRow =
            TableRow(mutableListOf<TableRecord<Any>>(
                TableRecord(TableColumn.Key, key.toInt())
            ).apply {
                addAll(tableRecords)
            })
    }
}

data class TableRecord<out T>(
    val tableColumn: TableColumn<T>,
    var data: @UnsafeVariance T,
) {
    override fun toString(): String =
        "[{${tableColumn.type}} $tableColumn]: $data"
}
interface TableColumns {
    val tableColumns: List<TableColumn<Any>>
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
                    row.tableRecords.firstOrNull { it.tableColumn == col }?.data?.toString()?.length ?: 0
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
                    tableRow.tableRecords.firstOrNull { it.tableColumn == col }?.data.toString()
                }
                appendLine(row(values))
            }
            append(separator())
        }
    }
    fun increment(tableRecords: List<TableRecord<Any>>) =
        tableRows
            .mapNotNull { row ->
                row.getRecord<Int>(TableColumn.Key)?.data
            }
            .maxOrNull()?.let { key ->
                tableRows.add(TableRow.increment("${key+1}", tableRecords))
            }
    fun increment(row: TableRow) = increment(row.tableRecords)
}
class RowBuilder(private val columns: List<TableColumn<Any>>) {
    private val values = mutableMapOf<TableColumn<*>, Any?>()
    val size: Int
        get() = values.size

    infix fun <T> TableColumn<T>.set(value: T) {
        values[this] = value
    }

    fun build(columns: List<TableColumn<*>>): TableRow {
        val records: List<TableRecord<Any>> = columns.mapNotNull { col ->
            val raw = values[col] ?: throw IllegalArgumentException("Column ${col.name} not found")
            val data = col.type.validate(raw)
            ((if (data != null)
                TableRecord(col, data)
            else
                null) as TableRecord<Any>?)
        }
        return TableRow(records.toMutableList())
    }
}
@Throws(IllegalArgumentException::class)
inline fun Table.newRow(block: RowBuilder.() -> Unit): TableRow {
    val builder = RowBuilder(tableColumns)
    builder.block()
    if (builder.size != tableColumns.size) {
        throw IllegalArgumentException("[RowBuilder.build]-[Table.newRow] New Row should have exactly size (expect: ${tableColumns.size}, actual: ${builder.size})")
    }
    return builder.build(tableColumns)
}