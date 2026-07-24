package org.example

import kotlin.random.Random

sealed class Transaction(val table: Table) {

    override fun hashCode(): Int = Random.hashCode()
    data class InsertRows(
        val t: Table,
        val rows: TransactionElement.Row
    ): Transaction(t)
    data class InsertRecords(
        val t: Table,
        val records: TransactionElement.Records
    ): Transaction(t)
    data class UpdateRows(
        val t: Table,
        val rows: TransactionElement.Row,
        val conditions: TransactionElement.Conditions
    ): Transaction(t)
    data class UpdateRecords(
        val t: Table,
        val records: TransactionElement.Records,
        val conditions: TransactionElement.Conditions
    ): Transaction(t)
    data class DeleteRows(
        val t: Table,
        val rows: TransactionElement.Row,
        val conditions: TransactionElement.Conditions
    ): Transaction(t)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return hashCode() == other.hashCode()
    }
}
sealed class TransactionElement {
    data class Row(var value: List<TableRow>)
    data class Records(val value: List<TableRecord<Any>>)
    data class Conditions(val value: List<Where<Any>>)
}