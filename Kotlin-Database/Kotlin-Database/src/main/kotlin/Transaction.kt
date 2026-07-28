package org.example

data class Transaction(
    val id: String,
    val operation: List<Operation> = listOf(),
)