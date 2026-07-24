package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Database {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val transactionFlow = MutableStateFlow<MutableList<Transaction>>(mutableListOf())

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

    private fun execute(t: Transaction) = t.table.apply {
        when (t) {
            is Transaction.InsertRows -> {
                t.rows.value.forEach { row -> insertRow(row) }
            }
            is Transaction.InsertRecords -> {
                insertRow(TableRow(t.records.value.toMutableList()))
            }
            is Transaction.UpdateRows -> {
                t.rows.value.forEach { row ->
                    dbUpdateRecord(row.tableRecords, t.conditions.value)
                }
            }
            is Transaction.UpdateRecords -> {
                dbUpdateRecord(t.records.value, t.conditions.value)
            }
            is Transaction.DeleteRows -> {
                t.conditions.value.forEach {
                    val data = it.data as? Int
                    if (it.tableColumn == TableColumn.Key && data != null)
                        deleteRow("$data")
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
