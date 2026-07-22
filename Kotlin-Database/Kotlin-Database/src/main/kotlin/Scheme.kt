package org.example

data class Column(
    val name: String,
    val dataType: DBDataType,
)

data class Row(
    val records: MutableList<Record>
) {
    override fun toString(): String =
        records.joinToString(", ")
}

data class Record(
    val column: Column,
    var data: String,
) {
    override fun toString(): String =
        "[{${column.dataType}} $column]: $data"
}

data class Table(
    val name: String,
    val columns: List<Column>,
    var rows: MutableList<Row>,
) {
    override fun toString(): String =
        "📦 Table: {$name}, Columns: " + columns.joinToString(", ") { "${it.name}[${it.dataType}]" } + "]"
}