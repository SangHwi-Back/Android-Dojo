package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RawSchema(
    @SerialName("table_name")
    val tableName: String,
    val columns: List<RawColumn>,
    val rows: List<JsonObject>
)

@Serializable
data class RawColumn(
    val name: String,
    val type: String,
)