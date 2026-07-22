package org.example

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
        val nameColumn = Column(name = "name", dataType = DBDataType.VARCHAR)
        val keyColumn = Column(name = "key", dataType = DBDataType.NUMBER)
        updateRecords(
            record = Record(column = nameColumn, data = "Colin"),
            where = listOf(Where(keyColumn, "3"))
        )
        deleteRow("4")
        deleteRow("5")
        insertRecords(columns.map {
            Record(column = it, data = "Testing")
        })
    }
    val fetchResult = users.selectRows()
    print(fetchResult)
}

fun MutableList<Row>.getRowIndex(key: String): Int? {
    for ((index, row) in this.withIndex()) {
        val keyRecord = row.records.firstOrNull { it.column.name == "key" }
        if (keyRecord != null && keyRecord.data == key)
            return index
    }
    return null
}
fun MutableList<Record>.getColumnIndex(column: Column): Int? {
    for ((index, record) in this.withIndex()) {
        if (record.column == column)
            return index
    }
    return null
}
fun MutableList<Record>.getRecord(column: Column): Record? {
    val columnIndex = getColumnIndex(column)
    return if (columnIndex != null) this[columnIndex] else null
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
