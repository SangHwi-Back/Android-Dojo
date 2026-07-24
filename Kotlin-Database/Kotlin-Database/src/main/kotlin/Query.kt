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
fun Table.insertRow(vararg rows: TableRow) {
    for (row in rows) {
        if (row.tableRecords.firstOrNull { it.tableColumn.name == TableColumn.Key.name } == null)
            row.appendKeyInRow(newKey())
        tableRows.add(row)
    }
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
fun <T : Any> Table.updateRecords(tableRecord: TableRecord<T>, where: List<Where<Any>>) {
    fun checkRow(tableRow: TableRow): Boolean {
        for ((tableColumn, value) in where) {
            val data = tableRow.tableRecords.firstOrNull { it.tableColumn == tableColumn }?.data
            if (data != null && value != data)
                return false
        }
        return true
    }

    tableRows.forEachIndexed { index, row ->
        if (checkRow(row).not()) return@forEachIndexed

        val columnIndex = row.tableRecords.indexOfFirst { it.tableColumn == tableRecord.tableColumn }
        if (columnIndex == -1) return@forEachIndexed

        tableRows[index].tableRecords[columnIndex] = TableRecord(
            tableRecord.tableColumn,
            tableRecord.tableColumn.type.validate(tableRecord.data))
    }
}
@Throws(IllegalArgumentException::class)
fun Table.deleteRow(key: String) {
    val rowIndex = tableRows.getRowIndex(key)
    if (rowIndex == null || rowIndex < 0)
        throw IllegalArgumentException("[Table.deleteRow($key)] No row found for key")
    tableRows.removeAt(rowIndex)
}