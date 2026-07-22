package org.example

fun main() {
    val users = Users().apply {
        println(this.tableRows)
        val nameTableColumn = TableColumn(name = "name", dataType = DBDataType.VARCHAR)
        val keyTableColumn = TableColumn(name = "key", dataType = DBDataType.NUMBER)
        updateRecords(
            tableRecord = TableRecord(tableColumn = nameTableColumn, data = "Colin"),
            where = listOf(Where(keyTableColumn, "3"))
        )
        deleteRow("4")
        deleteRow("5")
    }
    var fetchResult = users.selectRows()
    print(fetchResult)

    val environmentTable = EnvironmentTable()
    fetchResult = environmentTable.selectRows()
    print(fetchResult)
}

fun MutableList<TableRow>.getRowIndex(key: String): Int? {
    for ((index, row) in this.withIndex()) {
        val keyRecord = row.tableRecords.getRecord("key")
        if (keyRecord != null && keyRecord.data == key)
            return index
    }
    return null
}
fun MutableList<TableRecord>.getColumnIndex(name: String): Int? {
    for ((index, record) in this.withIndex()) {
        if (record.tableColumn.name == name)
            return index
    }
    return null
}
fun MutableList<TableRecord>.getRecord(columnName: String): TableRecord? {
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex != null) this[columnIndex] else null
}

enum class DBDataType {
    NUMBER,
    VARCHAR,
    DATE,
    EMAIL;
    // Interfaces
    override fun toString(): String = when (this) {
        NUMBER -> "Number"
        VARCHAR -> "Varchar"
        DATE -> "Date"
        EMAIL -> "Email"
    }
    fun checkType(data: String): Boolean = when (this) {
        NUMBER -> data.toIntOrNull() != null || data.toDoubleOrNull() != null
        VARCHAR -> true
        DATE -> {
            data.split("-").let {
                if (it.size < 3) return false
                for (num in it) {
                    if (num.toIntOrNull() == null) return false
                }
            }
            return true
        }
        EMAIL -> {
            val split1 = data.split("@")
            if (split1.size < 2) return false
            val split2 = split1[1].split(".")
            return split2.size >= 2
        }
    }
}
