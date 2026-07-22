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
    companion object {
        fun increment(key: String, records: List<Record>): Row =
            Row((mutableListOf(
                Record(
                    column = Column(name = "key", dataType = DBDataType.NUMBER),
                    data = key
                )
            ).apply {
                addAll(records)
            }))
    }
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
    fun increment(records: List<Record>) {
        val key = rows.mapNotNull { row ->
            row.records.getRecord("key")?.data?.toInt()
        }.maxOrNull() ?: -1
        rows.add(Row.increment("${key+1}", records))
    }
    inline fun columns(inlined: (List<Column>) -> Unit) {
        inlined(columns)
    }
}