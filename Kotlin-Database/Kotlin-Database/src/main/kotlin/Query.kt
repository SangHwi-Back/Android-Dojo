package org.example

fun Table.selectRows(
    columns: List<Column>? = null,
    where: List<Where> = listOf()
): List<Row> {
    fun selectAll(): List<Row> = rows.filter {
        for (condition in where) {
            for (record in it.records) {
                if (condition.column == record.column && condition.data != record.data) {
                    return@filter false
                }
            }
        }
        return@filter true
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
fun Table.insertRow(data: List<String>) {
    if (columns.size != data.size)
        throw IllegalArgumentException("Columns count don't match")
    rows.add(Row(records = columns.mapIndexed { index, column ->
        if (!column.dataType.checkType(data[index]))
            throw IllegalArgumentException("Column type ${column.dataType.name} not supported")
        Record(column, data[index])
    }.toMutableList()))
}
@Throws(IllegalArgumentException::class)
fun Table.insertRecords(records: List<Record>) {
    val records = records.toMutableList()
    val columnsExceptKeyRecord = columns.filter { column ->
        column.name.lowercase() != "key"
    }
    val recordsExceptKeyRecord = records.toMutableList().filter { record ->
        val column = record.column
        if (!column.dataType.checkType(record.data))
            throw IllegalArgumentException("Column type ${column.dataType.name} not supported")
        column.name.lowercase() != "key"
    }
    if (columnsExceptKeyRecord != recordsExceptKeyRecord)
        throw IllegalArgumentException("Columns not equals")
    increment(recordsExceptKeyRecord)
}
fun Table.updateRecords(record: Record, where: List<Where>) {
    fun checkRow(row: Row): Boolean {
        for (condition in where) {
            val data = row.records.firstOrNull { it.column == condition.column }?.data
            if (data != null && condition.data != data)
                return false
        }
        return true
    }
    rows.forEachIndexed { index, row ->
        if (checkRow(row).not()) return@forEachIndexed
        val columnIndex = row.records.indexOfFirst { it.column == record.column }
        if (columnIndex == -1) return@forEachIndexed
        rows[index].records[columnIndex].data = record.data
    }
}
fun Table.deleteRow(key: String) {
    val rowIndex = rows.getRowIndex(key)
    if (rowIndex == null || rowIndex < 0) return
    rows.removeAt(rowIndex)
}