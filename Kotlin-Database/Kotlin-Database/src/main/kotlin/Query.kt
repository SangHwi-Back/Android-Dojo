package org.example

fun Table.selectRows(
    tableColumns: List<TableColumn<Any>>? = null,
    where: List<Where<Any>> = listOf()
): List<TableRow> {
    fun selectAll(): List<TableRow> = tableRows.filter {
        for ((tableColumn, data) in where)
            for ((tableRecordColumn, tableRecordData) in it.tableRecords)
                if (tableColumn == tableRecordColumn && data != tableRecordData) {
                    return@filter false
                }
        return@filter true
    }
    fun selectFrom(targetColumns: List<TableColumn<Any>>): List<TableRow> {
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
fun Table.insertRow(row: TableRow) {
    if (row.tableRecords.firstOrNull { it.tableColumn == TableColumn.Key } == null)
        row.appendKeyInRow(newKey())
    increment(row)
}
fun TableRow.appendKeyInRow(key: Int) {
    if (tableRecords.firstOrNull { it.tableColumn == TableColumn.Key } != null) return
    tableRecords.addFirst(TableRecord(TableColumn.Key, key))
}
@Throws(NumberFormatException::class)
fun Table.newKey(): Int {
    if (tableRows.isEmpty())
        return 0
    return tableRows
        .maxBy { row ->
            row.tableRecords.firstOrNull { it.tableColumn == TableColumn.Key }?.data?.toString()?.toInt() ?: 0
        }
        .tableRecords.firstOrNull { it.tableColumn == TableColumn.Key }?.data?.toString()?.toInt() ?: 0
}
fun <T> Table.updateRecords(tableRecord: TableRecord<T>, where: List<Where<Any>>) {
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
        val data = tableRecord.tableColumn.type.validate(tableRecord.data)
        if (data != null) {
            tableRows[index].tableRecords[columnIndex].data = data
        }
    }
}
@Throws(IllegalArgumentException::class)
fun Table.deleteRow(key: String) {
    val rowIndex = tableRows.getRowIndex(key)
    if (rowIndex == null || rowIndex < 0)
        throw IllegalArgumentException("[Table.deleteRow($key)] No row found for key")
    tableRows.removeAt(rowIndex)
}