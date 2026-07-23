package org.example

data class Where<T>(
    val tableColumn: TableColumn<T>,
    val data: T,
)