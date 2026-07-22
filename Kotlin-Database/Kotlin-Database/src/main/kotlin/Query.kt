package org.example

fun Table.selectRows(
    tableColumns: List<TableColumn>? = null,
    where: List<Where> = listOf()
): List<TableRow> {
    fun selectAll(): List<TableRow> = tableRows.filter {
        for (condition in where) {
            for (record in it.tableRecords) {
                if (condition.tableColumn == record.tableColumn && condition.data != record.data) {
                    return@filter false
                }
            }
        }
        return@filter true
    }
    fun selectFrom(tableColumns: List<TableColumn>): List<TableRow> {
        val result = mutableListOf<TableRow>()
        outerLoop@ for (row in tableRows) {
            var row = row
            // Where 검증
            for (condition in where) {
                for (record in row.tableRecords) {
                    if (condition.tableColumn != record.tableColumn) {
                        continue@outerLoop
                    }
                }
            }
            // 원하는 컬럼만 추출
            row = TableRow(tableRecords = row.tableRecords.filter { tableColumns.contains(it.tableColumn) }.toMutableList())
            result.add(row)
        }
        return result
    }
    return if (tableColumns != null)
        selectFrom(tableColumns)
    else
        selectAll()
}

@Throws(IllegalArgumentException::class)
fun Table.insertRow(data: List<String>) {
    if (tableColumns.size != data.size)
        throw IllegalArgumentException("Columns count don't match")
    tableRows.add(TableRow(tableRecords = tableColumns.mapIndexed { index, column ->
        if (!column.dataType.checkType(data[index]))
            throw IllegalArgumentException("Column type ${column.dataType.name} not supported")
        TableRecord(column, data[index])
    }.toMutableList()))
}
@Throws(IllegalArgumentException::class)
fun Table.insertRecords(tableRecords: List<TableRecord>) {
    val records = tableRecords.toMutableList()
    val columnsExceptKeyRecord = tableColumns.filter { column ->
        column.name.lowercase() != "key"
    }
    val recordsExceptKeyRecord = records.toMutableList().filter { record ->
        val column = record.tableColumn
        if (!column.dataType.checkType(record.data))
            throw IllegalArgumentException("Column type ${column.dataType.name} not supported")
        column.name.lowercase() != "key"
    }
    if (columnsExceptKeyRecord != recordsExceptKeyRecord)
        throw IllegalArgumentException("Columns not equals")
    increment(recordsExceptKeyRecord)
}
fun Table.updateRecords(tableRecord: TableRecord, where: List<Where>) {
    fun checkRow(tableRow: TableRow): Boolean {
        for (condition in where) {
            val data = tableRow.tableRecords.firstOrNull { it.tableColumn == condition.tableColumn }?.data
            if (data != null && condition.data != data)
                return false
        }
        return true
    }
    tableRows.forEachIndexed { index, row ->
        if (checkRow(row).not()) return@forEachIndexed
        val columnIndex = row.tableRecords.indexOfFirst { it.tableColumn == tableRecord.tableColumn }
        if (columnIndex == -1) return@forEachIndexed
        tableRows[index].tableRecords[columnIndex].data = tableRecord.data
    }
}
fun Table.deleteRow(key: String) {
    val rowIndex = tableRows.getRowIndex(key)
    if (rowIndex == null || rowIndex < 0) return
    tableRows.removeAt(rowIndex)
}