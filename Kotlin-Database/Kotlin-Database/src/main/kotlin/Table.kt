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
    abstract val typeName: String
    protected abstract fun parseString(value: String): T?
    protected abstract fun cast(value: Any): T?
    // 검증 로직은 여기 한 곳에만 존재
    fun validate(value: Any?): T {
        val result = when (value) {
            null -> null
            is String -> parseString(value)
            else -> cast(value)
        }
        return result ?: throw IllegalArgumentException("Value must be a $typeName: $value")
    }

    companion object {
        fun string(type: String): TableColumnType<out Any> = when (type.lowercase()) {
            "number" -> NumberInt
            "date" -> DateTime
            "double" -> NumberDouble
            else -> Varchar
        }
    }

    override fun toString(): String = "TableColumnType.$typeName"

    object NumberInt : TableColumnType<Int>() {
        override val typeName = "integer"
        override fun parseString(value: String) = value.toIntOrNull()
        override fun cast(value: Any) = value as? Int
    }

    object NumberDouble : TableColumnType<Double>() {
        override val typeName = "double"
        override fun parseString(value: String) = value.toDoubleOrNull()
        override fun cast(value: Any) = value as? Double
    }

    object Varchar : TableColumnType<String>() {
        override val typeName = "string"
        override fun parseString(value: String): String = value
        override fun cast(value: Any) = value as? String
    }

    object DateTime : TableColumnType<Date>() {
        override val typeName = "date"
        override fun parseString(value: String): Date? =
            runCatching { Date.valueOf(value) }.getOrNull()
        override fun cast(value: Any) = value as? Date
    }
}

data class TableRow(
    val tableRecords: MutableList<TableRecord<Any>>
) {
    override fun toString(): String =
        tableRecords.joinToString(", ")

    inline fun <reified T> getRecords(): List<TableRecord<T>> {
        if (tableRecords.isEmpty()) return emptyList()
        @Suppress("UNCHECKED_CAST")
        return tableRecords
            .filter { it.dataValidateType() is T }
            .map { it as TableRecord<T> }
    }

    fun getKeyColumn(): TableRecord<Int>? {
        if (tableRecords.isEmpty()) return null
        @Suppress("UNCHECKED_CAST")
        return tableRecords
            .filter { it.isType<Int>() && it.tableColumn == TableColumn.Key }
            .map { it as TableRecord<Int> }
            .firstOrNull()
    }
}

data class TableRecord<T>(
    val tableColumn: TableColumn<T>,
    val data: T,
) {
    override fun toString(): String =
        "[{${tableColumn.type}} $tableColumn]: $data"

    fun dataValidateType(): T? = try {
        tableColumn.type.validate(data)
    } catch (_: IllegalArgumentException) {
        return null
    }

    inline fun <reified AnotherType> isType(): Boolean =
        dataValidateType() is AnotherType
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
    fun build(autoKey: Boolean): TableRow {
        val records: List<TableRecord<Any>> = columns.mapNotNull { col ->
            val rawData = values[col] ?: if (col.name == TableColumn.Key.name)
                if (autoKey)
                    maxKey + 1
                else
                    throw NoSuchElementException(TableColumn.Key.name)
            else
                throw IllegalArgumentException("Column ${col.name} not found $values")
            val data = col.type.validate(rawData)
            ((TableRecord(col, data)) as TableRecord<Any>?)
        }
        return TableRow(records.toMutableList())
    }
}

@Throws(IllegalArgumentException::class, NoSuchElementException::class)
inline fun Table.newRow(autoKey: Boolean = true, block: RowBuilder.() -> Unit): TableRow {
    val recordsKeyColumn = try {
        tableRows.mapNotNull { row ->
            row.tableRecords.firstOrNull { it.tableColumn == TableColumn.Key }?.data as? Int
        }.max()
    } catch (_: NoSuchElementException) {
        0
    }

    return RowBuilder(recordsKeyColumn, tableColumns).let {
        it.block()
        it.build(autoKey)
    }
}