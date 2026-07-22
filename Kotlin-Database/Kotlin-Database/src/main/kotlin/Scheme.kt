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
    override fun toString(): String {
        val colWidths = tableColumns.map { col ->
            maxOf(
                col.name.length,
                tableRows.maxOfOrNull { row ->
                    row.tableRecords.firstOrNull { it.tableColumn == col }?.data?.length ?: 0
                } ?: 0
            )
        }

        fun separator() = "+" + colWidths.joinToString("+") { "-".repeat(it + 2) } + "+"

        fun row(values: List<String>) =
            "|" + values.mapIndexed { i, v -> " ${v.padEnd(colWidths[i])} " }.joinToString("|") + "|"

        return buildString {
            appendLine(separator())
            appendLine(row(tableColumns.map { it.name }))
            appendLine(separator())
            for (tableRow in tableRows) {
                val values = tableColumns.map { col ->
                    tableRow.tableRecords.firstOrNull { it.tableColumn == col }?.data ?: ""
                }
                appendLine(row(values))
            }
            append(separator())
        }
    }
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