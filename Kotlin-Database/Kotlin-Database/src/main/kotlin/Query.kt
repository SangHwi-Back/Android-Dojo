package org.example

fun Table.selectRows(
    tableColumns: List<TableColumn>? = null,
    where: List<Where> = listOf()
): List<TableRow> {
    fun selectAll(): List<TableRow> = tableRows.filter {
        for ((tableColumn, data) in where)
            for ((tableRecordColumn, tableRecordData) in it.tableRecords)
                if (tableColumn == tableRecordColumn && data != tableRecordData) {
                    return@filter false
                }
        return@filter true
    }
    fun selectFrom(targetColumns: List<TableColumn>): List<TableRow> {
        return selectAll().map { row ->
            TableRow(row.tableRecords.filter {
                it.tableColumn in targetColumns
            }.toMutableList())
        }
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
    if (columnsExceptKeyRecord.map { it.name }.sorted() != recordsExceptKeyRecord.map { it.tableColumn.name }.sorted())
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