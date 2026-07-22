package org.example

data class TableColumn(
    val name: String,
    val dataType: DBDataType,
)

data class TableRow(
    val tableRecords: MutableList<TableRecord>
) {
    override fun toString(): String =
        tableRecords.joinToString(", ")
    companion object {
        fun increment(key: String, tableRecords: List<TableRecord>): TableRow =
            TableRow((mutableListOf(
                TableRecord(
                    tableColumn = TableColumn(name = "key", dataType = DBDataType.NUMBER),
                    data = key
                )
            ).apply {
                addAll(tableRecords)
            }))
    }
}

data class TableRecord(
    val tableColumn: TableColumn,
    var data: String,
) {
    override fun toString(): String =
        "[{${tableColumn.dataType}} $tableColumn]: $data"
}
interface TableColumns {
    val tableColumns: List<TableColumn>
}

interface TableRows {
    var tableRows: MutableList<TableRow>
}

abstract class Table(val name: String): TableColumns, TableRows {
    override fun toString(): String =
        "📦 Table: {$name}, Columns: " + tableColumns.joinToString(", ") { "${it.name}[${it.dataType}]" } + "]"
    fun increment(tableRecords: List<TableRecord>) {
        val key = tableRows.mapNotNull { row ->
            row.tableRecords.getRecord("key")?.data?.toInt()
        }.maxOrNull() ?: -1
        tableRows.add(TableRow.increment("${key+1}", tableRecords))
    }
    inline fun columns(inlined: (List<TableColumn>) -> Unit) {
        inlined(tableColumns)
    }
}