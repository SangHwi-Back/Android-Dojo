package org.example

fun Table.selectRows(
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
fun Table.insertRow(data: List<String>) {
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
fun Table.insertRecords(data: List<Record>) {
    if (columns.map { it.name.lowercase() }.sorted() != data.map { it.column.name.lowercase() }.sorted())
        throw IllegalArgumentException("Columns not equals")
    rows.add(Row(records = data.toMutableList()))
}
@Throws(NoSuchElementException::class)
fun Table.updateRecord(key: String, data: String, column: String) {
    val rowIndex = rows.getRowIndex(key)
    if (rowIndex == null || rowIndex < 0) return

    val columnIndex = rows[rowIndex].records.map { it.column.name }.indexOf(column)
    if (columnIndex < 0) return

    rows[rowIndex].records[columnIndex].data = data
}
@Throws(NoSuchElementException::class)
fun Table.updateRecord(row: Row) {
    val rowIndex = rows.indexOf(row)
    if (rowIndex < 0) return
    rows[rowIndex] = row
}
@Throws(NoSuchElementException::class)
fun Table.updateRecord(key: String, record: Record) {
    val rowIndex = rows.getRowIndex(key)
    if (rowIndex == null || rowIndex < 0) return

    val columnIndex = rows[rowIndex].records.getColumnIndex(record.column)
    if (columnIndex == null || columnIndex < 0) return

    rows[rowIndex].records[columnIndex] = record
}
fun Table.deleteRow(key: String) {
    val rowIndex = rows.getRowIndex(key)
    if (rowIndex == null || rowIndex < 0) return
    rows.removeAt(rowIndex)
}