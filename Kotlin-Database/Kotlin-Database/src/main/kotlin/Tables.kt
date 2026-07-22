package org.example

class Users: Table("Users") {
    init {
        insertRow(listOf("1", "John", "2027-07-20", "john@gmail.com"))
        insertRow(listOf("2", "Rose", "2027-06-18", "rose@gmail.com"))
        insertRow(listOf("3", "Smith", "2027-03-20", "smith@gmail.com"))
        insertRow(listOf("4", "Sally", "2027-04-28", "sally@gmail.com"))
        insertRow(listOf("5", "David", "2027-12-20", "david@gmail.com"))
    }
    override val tableColumns: List<TableColumn>
        get() = listOf(
            TableColumn("key", DBDataType.NUMBER),
            TableColumn("name", DBDataType.VARCHAR),
            TableColumn("birth", DBDataType.DATE),
            TableColumn("email", DBDataType.VARCHAR)
        )
    override var tableRows: MutableList<TableRow> = mutableListOf()
}

class EnvironmentTable: Table("Environment") {
    val nameColumn = TableColumn("name", DBDataType.VARCHAR)
    val valueColumn = TableColumn("value", DBDataType.VARCHAR)
    override val tableColumns: List<TableColumn>
        get() = listOf(nameColumn, valueColumn)
    override var tableRows: MutableList<TableRow> = mutableListOf(
        TableRow(mutableListOf(
            TableRecord(nameColumn, "version"),
            TableRecord(valueColumn, "1.0"),
        ))
    )
}

