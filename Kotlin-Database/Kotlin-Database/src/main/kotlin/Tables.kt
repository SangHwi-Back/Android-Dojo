package org.example

class Users: Table("Users") {
    override var tableRows: MutableList<TableRow> = mutableListOf()
    override val tableColumns: List<TableColumn<Any>>
        get() = allColumns
    companion object {
        val keyColumn = TableColumn("key", TableColumnType.NumberInt)
        val nameColumn = TableColumn("name", TableColumnType.Varchar)
        val birthColumn = TableColumn("birth", TableColumnType.DateTime)
        val emailColumn = TableColumn("email", TableColumnType.Varchar)
        val allColumns = listOf(keyColumn, nameColumn, birthColumn, emailColumn)
    }
}

class EnvironmentTable: Table("Environment") {
    override val tableColumns: List<TableColumn<Any>>
        get() = allColumns
    override var tableRows: MutableList<TableRow> = mutableListOf(
        TableRow(mutableListOf(
            TableRecord(nameColumn, "version"),
            TableRecord(valueColumn, "1.0"),
        ))
    )
    companion object {
        val nameColumn = TableColumn("name", TableColumnType.Varchar)
        val valueColumn = TableColumn("value", TableColumnType.Varchar)
        val allColumns = listOf(nameColumn, valueColumn)
    }
}

