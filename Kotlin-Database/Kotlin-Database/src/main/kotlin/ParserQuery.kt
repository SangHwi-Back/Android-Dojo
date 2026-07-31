package org.example

class ParserQuery(private val rawQueryString: String) {
    var query = ""
    val command: Token.Command
    val columnsEffect: List<Token.Identifier.Column>
    val tables: List<Token.Identifier.Table>
    val dmlData: Token.Data?

    init {
        removeUnused()
        command = getCommand()
        columnsEffect = getColumnIdentifiers()
        getTargetTable().let {
            tables = if (it != null) listOf(it) else listOf()
        }
        dmlData = getDMLData()
    }

    private fun removeUnused() {
        val rawQuery = rawQueryString.substringBefore(';')
        for (line in rawQuery.lines()) {
            query += line.trim() // 양 옆 띄어쓰기 제거
                .replace("\\s+".toRegex(), " ") // 중간에 띄어쓰기 중 두 칸 이상 한칸으로 고정
                .plus(" ")
        }
        // 앞 뒤 공백과 맨 뒤 ; 제거
        query = query.trim().removeSuffix(";")
    }

    private fun getCommand(): Token.Command {
        val items = query.split(Regex("\\s+")).toMutableList()
        if (items.isEmpty())
            throw IllegalArgumentException("No Keyword Found")
        var keyword = items.removeFirst().uppercase()
        if (keyword == "INSERT" || keyword == "DELETE")
            keyword += " ${items.removeFirst().uppercase()}"
        return when (keyword) {
            "SELECT" -> Token.Command.SELECT
            "UPDATE" -> Token.Command.UPDATE
            "DELETE FROM" -> Token.Command.DELETE
            "INSERT INTO" -> Token.Command.INSERT
            else -> throw IllegalArgumentException("Unknown keyword")
        }
    }

    private fun getColumnIdentifiers(): List<Token.Identifier.Column> {
        var sql = removeKeyword(query).trim()
        return when (command) {
            Token.Command.SELECT -> {
                sql = sql.indexOf("from", ignoreCase = true).let { index ->
                    if (index == -1) return emptyList() else sql.removeRange(index, sql.length)
                }
            }
            Token.Command.UPDATE -> {
                sql = sql.indexOf("set", ignoreCase = true).let { setIndex ->
                    if (setIndex == -1) return emptyList()
                    val end = sql.indexOf("where", ignoreCase = true)
                    if (end == -1)
                        sql.substring(setIndex + 3)
                    else
                        sql.substring(setIndex + 3, end)
                }
            }
            Token.Command.INSERT -> {
                sql = sql.indexOf("(", 0).let { openIndex ->
                    if (openIndex == -1) return emptyList() else
                        sql.indexOf(")").let { closeIndex ->
                            if (closeIndex == -1) return emptyList() else sql.substring(openIndex + 1, closeIndex)
                        }
                }
            }
            Token.Command.DELETE ->
                return emptyList()
        }.let {
            return if (command == Token.Command.UPDATE) {
                sql.trim().split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
                    .map { Token.Identifier.Column(it.trim().removeSuffix(",")) }
            } else {
                sql.split(Regex("\\s+"))
                    .toMutableList()
                    .map { Token.Identifier.Column(it.trim().removeSuffix(",")) }
            }
        }
    }

    private fun getTargetTable(): Token.Identifier.Table? = when (command) {
        Token.Command.SELECT, Token.Command.DELETE -> {
            val fromIndex = query.indexOf("from", ignoreCase = true)
            if (fromIndex == -1) null else query
                .removeRange(0, query.indexOf("from", 0, true) + 4)
                .removePrefix(" ")
                .split(Regex("\\s+"))
                .firstOrNull()
                ?.let { Token.Identifier.Table(it) }
        }
        Token.Command.UPDATE, Token.Command.INSERT -> {
            removeKeyword(query)
                .split(Regex("\\s+"))
                .firstOrNull()
                ?.let { Token.Identifier.Table(it) }
        }
    }
    private fun getDMLData(): Token.Data? = when (command) {
        Token.Command.INSERT -> {

            Token.Data.Insert(query
                .indexOf("values", 0, true).let { valuesIndex ->

                    if (valuesIndex == -1) return null else query
                        .indexOf('(', valuesIndex).let { openIndex ->

                            if (openIndex == -1) return null else query
                                .indexOf(')', openIndex).let { closeIndex ->

                                    if (closeIndex == -1) return null else query
                                        .substring(openIndex + 1, closeIndex)
                                        .replace("\\s+".toRegex(), " ")
                                        .split(",")
                                        .mapIndexed { index, string ->
                                            Pair(columnsEffect[index].name, string)
                                        }
                            }
                        }
                }
            )
        }
        Token.Command.DELETE, Token.Command.SELECT -> {
            null
        }
        Token.Command.UPDATE -> {
            Token.Data.Update(query
                .indexOf("set", ignoreCase = true).let { setIndex ->
                    if (setIndex == -1) return null else query.indexOf("where", ignoreCase = true).let { whereIndex ->
                        if (whereIndex == -1)
                            query.removeRange(0, setIndex+3).trim()
                                .split(",")
                                .mapNotNull { data ->
                                    data.split("=").map { it.trim() }.let {
                                        if (it.size == 2)
                                            Pair(it[0], it[1])
                                        else
                                            null
                                    }
                                }
                        else if (setIndex < whereIndex)
                            query.substring(setIndex+3, whereIndex).trim()
                                .split(",")
                                .mapNotNull { data ->
                                    data.split("=").map { it.trim() }.let {
                                        if (it.size == 2)
                                            Pair(it[0], it[1])
                                        else
                                            null
                                    }
                                }
                        else
                            return null
                    }
                })
        }
    }
}

sealed class Token {
    enum class Command {
        SELECT, INSERT, DELETE, UPDATE
    }
    sealed class Identifier: Token() {
        data class Column(val name: String) : Identifier()
        data class Table(val name: String) : Identifier()
    }
    sealed class Data: Token() {
        data class Insert(val values: List<Pair<String, String>>): Data()
        data class Update(val values: List<Pair<String, String>>): Data()
    }
    // MARK: 아직 사용 안하는 중. 있어야 하는지 아닌지 모르곘음.
    sealed class Operator: Token() {
        data class Equal(val left: String, val right: String) : Operator()
        data class NotEqual(val left: String, val right: String) : Operator()
        data class And(val left: String, val right: String) : Operator()
        data class Or(val left: String, val right: String) : Operator()
        /**
         * left > right or left >= right
         */
        data class LessThan(val left: String, val right: String, val containsEqual: Boolean = false) : Operator()
        /**
         * left < right or left <= right
         */
        data class GreaterThan(val left: String, val right: String, val containsEqual: Boolean = false) : Operator()
        data class LeftJoin(val left: String, val right: String, val on: String) : Operator()
        data class RightJoin(val left: String, val right: String, val on: String) : Operator()
    }
}
fun removeKeyword(sql: String): String {
    fun String.removeIfCommonPrefixWith(other: String): Pair<String, Boolean> = commonPrefixWith(other, true).let {
        if (!it.isEmpty())
            return Pair(removePrefix(it).removePrefix(" "), true)
        return Pair(this, false)
    }
    // SELECT 제거
    sql.removeIfCommonPrefixWith("SELECT").apply { if (second) return first }
    // UPDATE 제거
    sql.removeIfCommonPrefixWith("UPDATE").apply { if (second) return first }
    // INSERT INTO 제거
    sql.removeIfCommonPrefixWith("INSERT").apply {
        if (!second) return@apply
        first.removeIfCommonPrefixWith("INTO").apply { if (second) return first }
    }
    // DELETE FROM 제거
    sql.removeIfCommonPrefixWith("DELETE").apply {
        if (!second) return@apply
        first.removeIfCommonPrefixWith("FROM").apply { if (second) return first }
    }
    return sql
}