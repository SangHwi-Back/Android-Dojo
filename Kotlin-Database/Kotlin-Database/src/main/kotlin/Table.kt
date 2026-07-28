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

    override fun toString(): String =
        "name is $name, type is $type"

    companion object {
        val Key = TableColumn("key", TableColumnType.NumberInt)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}

sealed class TableColumnType<T> {
    abstract fun validate(value: Any?): T

    companion object {
        fun string(type: String): TableColumnType<out Any> = when (type.lowercase()) {
            "number" -> NumberInt
            "date" -> DateTime
            "double" -> NumberDouble
            else -> Varchar
        }
    }

    object NumberInt: TableColumnType<Int>() {
        override fun validate(value: Any?): Int = if (value is String)
            value.toIntOrNull() ?: throw IllegalArgumentException("Value must be a integer $value.")
        else
            value as? Int ?: throw IllegalArgumentException("Value must be a integer $value.")

        override fun toString(): String = "TableColumnType.NumberInt"
    }
    object NumberDouble: TableColumnType<Double>() {
        override fun validate(value: Any?): Double = if (value is String)
            value.toDoubleOrNull() ?: throw IllegalArgumentException("Value must be a integer $value.")
        else
            value as? Double ?: throw IllegalArgumentException("Value must be a float or double $value.")

        override fun toString(): String = "TableColumnType.NumberDouble"
    }
    object Varchar: TableColumnType<String>() {
        override fun validate(value: Any?): String =
            value as? String ?: throw IllegalArgumentException("Value is not a string $value.")

        override fun toString(): String = "TableColumnType.Varchar"
    }
    object DateTime: TableColumnType<Date>() {
        override fun validate(value: Any?): Date = if (value is String)
            Date.valueOf(value) ?: throw IllegalArgumentException("Value must be a integer $value.")
        else
            value as? Date ?: throw IllegalArgumentException("Date cannot be converted $value.")

        override fun toString(): String = "TableColumnType.DateTime"
    }
}

data class TableRow(
    val tableRecords: MutableList<TableRecord<Any>>
) {
    override fun toString(): String =
        tableRecords.joinToString(", ")
    companion object {
        fun addRowWithKey(key: String, tableRecords: List<TableRecord<Any>>): TableRow =
            TableRow(mutableListOf<TableRecord<Any>>(
                TableRecord(TableColumn.Key, key.toInt())
            ).apply {
                addAll(tableRecords)
            })
    }
}

data class TableRecord<out T>(
    val tableColumn: TableColumn<T>,
    val data: T,
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
}

class RowBuilder(private val maxKey: Int, private val columns: List<TableColumn<Any>>) {
    private val values = mutableMapOf<TableColumn<*>, Any?>()
    val size: Int
        get() = values.size

    infix fun <T> TableColumn<T>.set(value: T) {
        values[this] = value
    }

    @Throws(IllegalArgumentException::class, NoSuchElementException::class)
    fun build(): TableRow {
        val records: List<TableRecord<Any>> = columns.mapNotNull { col ->
            val raw = values[col] ?: if (col.name == TableColumn.Key.name)
                maxKey + 1
            else
                throw IllegalArgumentException("Column ${col.name} not found $values")
            val data = col.type.validate(raw)
            ((TableRecord(col, data)) as TableRecord<Any>?)
        }
        return TableRow(records.toMutableList())
    }
}

@Throws(IllegalArgumentException::class, NoSuchElementException::class)
inline fun Table.newRow(block: RowBuilder.() -> Unit): TableRow {
    val recordsKeyColumn = try {
        tableRows.mapNotNull { row ->
            row.tableRecords.firstOrNull { it.tableColumn == TableColumn.Key }?.data as? Int
        }.max()
    } catch (_: NoSuchElementException) {
        0
    }
    val builder = RowBuilder(recordsKeyColumn, tableColumns)
    builder.block()
    return try {
        builder.build()
    } catch (exception: NoSuchElementException) {
        if (exception.message == TableColumn.Key.name) {
            // TODO: Set New Key Needed
            builder.build()
        } else {
            throw exception
        }
    }
}

@Throws(IllegalArgumentException::class, NoSuchElementException::class)
inline fun Table.addNewRow(block: RowBuilder.() -> Unit): TableRow {
    val newRow = newRow(block)
    tableRows.add(newRow)
    return newRow
}