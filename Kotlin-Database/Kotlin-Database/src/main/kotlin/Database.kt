package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class Database {
    private val scope = CoroutineScope(Dispatchers.IO)
    val tables = mutableMapOf<String, Table>()

    private val transactionFlow = MutableStateFlow<MutableList<Transaction>>(mutableListOf())

    private val _selectTransactionFlow = MutableStateFlow(listOf<TableRow>())
    val selectTransactionFlow = _selectTransactionFlow.asStateFlow()

    init {
        scope.launch {
            transactionFlow.asStateFlow().collect { list ->
                list.forEach { t ->
                    execute(t)
                }
            }
        }
    }

    fun insertTransaction(vararg transactions: Transaction) {
        transactionFlow.update { transactions.toMutableList() }
    }

    private fun execute(t: Transaction) {
        val operations = t.operation.mapNotNull {
            val table = tables[it.tableName]
            if (table != null) Pair(table, it) else null
        }

        for ((table, operation) in operations) {
            when (operation) {
                is Operation.Select -> _selectTransactionFlow.update {
                    table.selectRows(operation.columns)
                }
                is Operation.Insert ->
                    table.insertRow(operation.row)
                is Operation.Update ->
                    table.dbUpdateRecord(operation.records, operation.where)
                is Operation.Delete -> when (operation.condition) {
                    is ConditionDelete.ID ->
                        table.deleteRow(operation.condition.id.toString())
                    is ConditionDelete.WHERE -> if (operation.condition.where.tableColumn == TableColumn.Key) {
                        operation.condition.where.apply {
                            table.deleteRow(
                                tableColumn.type.validate(data).toString()
                            )
                        }
                    }
                }
            }
        }
    }
    private fun Table.dbUpdateRecord(tableRecords: List<TableRecord<Any>>, where: List<Where<Any>>) {
        tableRecords.forEach {
            updateRecords(it, where)
        }
    }
}
