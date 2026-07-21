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
    fun selectRows(
        columns: List<Column>? = null,
        where: List<Where> = listOf()
    ): List<Row> {
        fun selectAll(): List<Row> = rows.filter {
            for (condition in where) {
                for (record in it.records) {
                    if (condition.column == record.column) {
                        return@filter condition.data == record.data
                    }
                }
            }
            return@filter false
        }
        fun selectFrom(columns: List<Column>): List<Row> {
            val result = mutableListOf<Row>()
            outerLoop@ for (row in rows) {
                var row = row
                // Where 검증
                for (condition in where) {
                    for (record in row.records) {
                        if (condition.column != record.column) {
                            continue@outerLoop
                        }
                    }
                }
                // 원하는 컬럼만 추출
                row = Row(records = row.records.filter { columns.contains(it.column) }.toMutableList())
                result.add(row)
            }
            return result
        }
        return if (columns != null)
            selectFrom(columns)
        else
            selectAll()
    }
    @Throws(IllegalArgumentException::class)
    fun insertRow(data: List<String>) {
        if (columns.size != data.size)
            throw IllegalArgumentException("Columns count don't match")
        rows.add(Row(records = columns.mapIndexed { index, column ->
            Record(
                column,
                data[index],
                dataType = column.dataType
            )
        }.toMutableList()))
    }
    @Throws(IllegalArgumentException::class)
    fun insertRecords(data: List<Record>) {
        if (columns.map { it.name.lowercase() }.sorted() != data.map { it.column.name.lowercase() }.sorted())
            throw IllegalArgumentException("Columns not equals")
        rows.add(Row(records = data.toMutableList()))
    }
    @Throws(NoSuchElementException::class)
    fun updateRecord(key: String, data: String, column: String) {
        val rowIndex = rows.getRowIndex(key)
        if (rowIndex == null || rowIndex < 0) return

        val columnIndex = rows[rowIndex].records.map { it.column.name }.indexOf(column)
        if (columnIndex < 0) return

        rows[rowIndex].records[columnIndex].data = data
    }
    @Throws(NoSuchElementException::class)
    fun updateRecord(row: Row) {
        val rowIndex = rows.indexOf(row)
        if (rowIndex < 0) return
        rows[rowIndex] = row
    }
    @Throws(NoSuchElementException::class)
    fun updateRecord(key: String, record: Record) {
        val rowIndex = rows.getRowIndex(key)
        if (rowIndex == null || rowIndex < 0) return

        val columnIndex = rows[rowIndex].records.getColumnIndex(record.column)
        if (columnIndex == null || columnIndex < 0) return

        rows[rowIndex].records[columnIndex] = record
    }
    fun deleteRow(key: String) {
        val rowIndex = rows.getRowIndex(key)
        if (rowIndex == null || rowIndex < 0) return
        rows.removeAt(rowIndex)
    }
}
fun MutableList<Row>.getRowIndex(key: String): Int? {
    for ((index, row) in this.withIndex()) {
        val record = row.records.firstOrNull { it.column.name == "key" }
        if (record != null && record.data == key)
            return index
    }
    return null
}
fun MutableList<Record>.getColumnIndex(key: Column): Int? {
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
    val column: Column,
    var data: String,
    val dataType: DBDataType,
) {
    override fun toString(): String =
        "[{$dataType} $column]: $data"
}
data class Where(
    val column: Column,
    val data: String,
)
enum class DBDataType {
    NUMBER, VARCHAR, DATE, EMAIL;

    override fun toString(): String = when (this) {
        NUMBER -> "Number"
        VARCHAR -> "Varchar"
        DATE -> "Date"
        EMAIL -> "Email"
    }
}
