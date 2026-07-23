package org.example

class Users: Table("Users") {
    override var tableRows: MutableList<TableRow> = mutableListOf()
    override val tableColumns: List<TableColumn>
        get() = allColumns
    companion object {
        val keyColumn = TableColumn(name = "key", dataType = DBDataType.NUMBER)
        val nameColumn = TableColumn(name = "name", dataType = DBDataType.VARCHAR)
        val birthColumn = TableColumn("birth", DBDataType.DATE)
        val emailColumn = TableColumn("email", DBDataType.VARCHAR)
        val allColumns = listOf(keyColumn, nameColumn, birthColumn, emailColumn)
    }
}

class EnvironmentTable: Table("Environment") {
    override val tableColumns: List<TableColumn>
        get() = allColumns
    override var tableRows: MutableList<TableRow> = mutableListOf(
        TableRow(mutableListOf(
            TableRecord(nameColumn, "version"),
            TableRecord(valueColumn, "1.0"),
        ))
    )
    companion object {
        val nameColumn = TableColumn("name", DBDataType.VARCHAR)
        val valueColumn = TableColumn("value", DBDataType.VARCHAR)
        val allColumns = listOf(nameColumn, valueColumn)
    }
}

