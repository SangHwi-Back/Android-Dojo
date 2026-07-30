package org.example

sealed interface Operation {
    val tableName: String
    data class Select(
        override val tableName: String,
        val columns: List<TableColumn<Any>>, val where: List<Where<Any>> = listOf()
    ): Operation
    data class Insert(
        override val tableName: String,
        val row: TableRow
    ) : Operation
    data class Delete(
        override val tableName: String,
        val condition: ConditionDelete
    ) : Operation
    data class Update(
        override val tableName: String,
        val records: List<TableRecord<Any>>, val where: List<Where<Any>>
    ) : Operation
}

sealed class ConditionDelete {
    data class ID(val id: Int) : ConditionDelete()
    data class WHERE(val where: Where<Any>) : ConditionDelete()
}

data class Where<T>(
    val tableColumn: TableColumn<T>,
    val data: T,
)