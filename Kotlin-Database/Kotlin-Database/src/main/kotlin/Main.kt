package org.example

import kotlin.jvm.Throws
import kotlin.text.lowercase

fun main() {
    val users = Table(
        name = "Users",
        columns = listOf(
            Column("key", DBDataType.NUMBER),
            Column("name", DBDataType.VARCHAR),
            Column("birth", DBDataType.DATE),
            Column("email", DBDataType.VARCHAR)
        ),
        rows = mutableListOf()
    ).apply {
        println(this)
        insertRow(listOf("1", "John", "2027-07-20", "john@gmail.com"))
        insertRow(listOf("2", "Rose", "2027-06-18", "rose@gmail.com"))
        insertRow(listOf("3", "Smith", "2027-03-20", "smith@gmail.com"))
        insertRow(listOf("4", "Sally", "2027-04-28", "sally@gmail.com"))
        insertRow(listOf("5", "David", "2027-12-20", "david@gmail.com"))
    }.apply {
        println(this.rows)
        updateRecord("3","Colin", "name")
        deleteRow("4")
        deleteRow("5")
    }
    print(users.rows)
}

data class Table(
    val name: String,
    val columns: List<Column>,
    var rows: MutableList<Row>,
) {
    override fun toString(): String =
        "📦 Table: {$name}, Columns: " + columns.joinToString(", ") { "${it.name}[${it.dataType}]" } + "]"
    @Throws(IllegalArgumentException::class)
    fun insertRow(data: List<String>) {
        if (columns.size != data.size)
            throw IllegalArgumentException("Columns count don't match")
        rows.add(Row(records = columns.mapIndexed { index, column ->
            Record(
                column.name,
                data[index],
                dataType = column.dataType
            )
        }.toMutableList()))
    }
    @Throws(IllegalArgumentException::class)
    fun insertRecords(data: List<Record>) {
        if (columns.map { it.name.lowercase() }.sorted() != data.map { it.column.lowercase() }.sorted())
            throw IllegalArgumentException("Columns not equals")
        rows.add(Row(records = data.toMutableList()))
    }
    @Throws(NoSuchElementException::class)
    fun updateRecord(key: String, data: String, column: String) {
        val rowIndex = rows.getRowIndex(key)
        if (rowIndex == null || rowIndex < 0) return

        val columnIndex = rows[rowIndex].records.getColumnIndex(column)
        if (columnIndex == null || columnIndex < 0) return

        rows[rowIndex].records[columnIndex].data = data
    }
    fun deleteRow(key: String) {
        val rowIndex = rows.getRowIndex(key)
        if (rowIndex == null || rowIndex < 0) return
        rows.removeAt(rowIndex)
    }
}
fun MutableList<Row>.getRowIndex(key: String): Int? {
    for ((index, row) in this.withIndex()) {
        val record = row.records.firstOrNull { it.column == "key" }
        if (record != null && record.data == key)
            return index
    }
    return null
}
fun MutableList<Record>.getColumnIndex(key: String): Int? {
    for ((index, record) in this.withIndex()) {
        if (record.column == key)
            return index
    }
    return null
}
data class Column(
    val name: String,
    val dataType: DBDataType,
)

data class Row(
    val records: MutableList<Record>
) {
    override fun toString(): String =
        records.joinToString(", ")
}

data class Record(
    val column: String,
    var data: String,
    val dataType: DBDataType,
) {
    override fun toString(): String =
        "[{$dataType} $column]: $data"
}

enum class DBDataType {
    NUMBER, VARCHAR, DATE, EMAIL;

    override fun toString(): String = when (this) {
        NUMBER -> "Number"
        VARCHAR -> "Varchar"
        DATE -> "Date"
        EMAIL -> "Email"
    }
}
