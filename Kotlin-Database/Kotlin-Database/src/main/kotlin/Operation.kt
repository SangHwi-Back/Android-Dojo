package org.example

sealed interface Operation {
    val tableName: String
    data class Select(val _tableName: String, val columns: List<TableColumn<Any>>): Operation {
        override val tableName: String
            get() = _tableName
    }
    data class Insert(val _tableName: String, val row: TableRow) : Operation {
        override val tableName: String
            get() = _tableName
    }
    data class Delete(val _tableName: String, val condition: ConditionDelete) : Operation {
        override val tableName: String
            get() = _tableName
    }
    data class Update(val _tableName: String, val records: List<TableRecord<Any>>, val where: List<Where<Any>>) : Operation {
        override val tableName: String
            get() = _tableName
    }
}

sealed class ConditionDelete {
    data class ID(val id: Int) : ConditionDelete()
    data class WHERE(val where: Where<Any>) : ConditionDelete()
}

data class Where<T>(
    val tableColumn: TableColumn<T>,
    val data: T,
)