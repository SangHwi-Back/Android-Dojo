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
        tables = listOf(getTargetTable())
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

    @Throws(IllegalArgumentException::class)
    private fun getCommand(): Token.Command {
        val items = query.split(Regex("\\s+")).toMutableList()
        if (items.isEmpty())
            throw IllegalArgumentException("No Keyword Found $query")
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
            Token.Command.DELETE ->
                return emptyList()
            Token.Command.SELECT -> {
                val fromIndex = sql.indexOf("from", ignoreCase = true)
                if (fromIndex == -1)
                    return emptyList()
                else
                    sql = sql.removeRange(fromIndex, sql.length)
            }
            Token.Command.UPDATE -> {
                var setIndex = sql.indexOf("set", ignoreCase = true)
                if (setIndex == -1)
                    return emptyList()

                setIndex += 3

                val end = sql.indexOf("where", setIndex, true)

                sql = sql.apply {
                    if (end == -1)
                        substring(setIndex)
                    else
                        substring(setIndex, end)
                }
            }
            Token.Command.INSERT -> {
                sql = listOf(
                    sql.indexOf('('),
                    sql.indexOf(')')
                ).let { indexes ->
                    if (indexes[0] == -1 || indexes[1] == -1)
                        return emptyList()
                    else
                        sql.substring(indexes[0] +1, indexes[1])
                }
            }
        }.let {
            var split = sql.trim().split(Regex("\\s+"))
            if (command == Token.Command.UPDATE)
                split = split.filter { it.isNotBlank() }
            split.map {
                Token.Identifier.Column(it.trim().removeSuffix(","))
            }
        }
    }

    private fun getTargetTable(): Token.Identifier.Table = when (command) {
        Token.Command.SELECT, Token.Command.DELETE -> {
            val fromIndex = query.indexOf("from", ignoreCase = true)
            if (fromIndex == -1)
                throw IllegalArgumentException("No Keyword Select From $query")
            else query
                .removeRange(0, fromIndex + 4)
                .removePrefix(" ")
                .split(Regex("\\s+"))
                .firstOrNull()
                .let {
                    if (it != null)
                        Token.Identifier.Table(it)
                    else
                        throw IllegalArgumentException("No Table Found $query")
                }
        }
        Token.Command.UPDATE, Token.Command.INSERT -> {
            removeKeyword(query)
                .split(Regex("\\s+"))
                .firstOrNull()
                .let {
                    if (it != null)
                        Token.Identifier.Table(it)
                    else
                        throw IllegalArgumentException("No Table Found $query")
                }
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun getDMLData(): Token.Data? = when (command) {
        Token.Command.DELETE, Token.Command.SELECT -> {
            return null
        }
        Token.Command.INSERT -> {
            var valuesIndex = query.indexOf("values", 0, true)
            if (valuesIndex == -1)
                throw IllegalArgumentException("No Keyword Insert Values $query")
            valuesIndex += 6

            var open = query.indexOf('(', valuesIndex)
            val close = query.indexOf(')', open)
            if (open == -1 || close == -1 || open >= close)
                throw IllegalArgumentException("Invalid Insert Data after Values keyword [${query.substring(valuesIndex)}]")
            open += 1

            return Token.Data.Insert(query
                .substring(open, close)
                .split(",")
                .mapIndexed { i, data ->
                    listOf(
                        data.indexOfFirst { it == '\''},
                        data.indexOfLast { it == '\''}
                    ).let { indexes ->
                        if (indexes[0] == -1 || indexes[1] == -1)
                            throw IllegalArgumentException("Insert value clause has problem [${query.substring(open, close)}]")
                        else
                            Pair(columnsEffect[i].name, data.substring(indexes[0]+1, indexes[1]))
                    }
                }
            )
        }
        Token.Command.UPDATE -> {
            var setIndex = query.indexOf("set", ignoreCase = true)
            if (setIndex == -1)
                throw IllegalArgumentException("No Keyword Update Set $query")
            setIndex += 3

            val whereIndex = query.indexOf("where", setIndex, true)
            val end = if (whereIndex == -1) setIndex else whereIndex

            if (setIndex >= end)
                throw IllegalArgumentException("Invalid Update Data After Set clause [${query.substring(setIndex)}]")

            return Token.Data.Update(query
                .removeRange(setIndex, end)
                .trim()
                .split(",")
                .mapNotNull { data ->
                    data.split("=").map { it.trim() }.let {
                        if (it.size == 2)
                            listOf(
                                it[1].indexOfFirst { char -> char == '\'' },
                                it[1].indexOfLast { char -> char == '\'' }
                            ).let { indexes ->
                                if (indexes[0] == -1 || indexes[1] == -1)
                                    null
                                else
                                    Pair(it[0], it[1].substring(indexes[0]+1, indexes[1]))
                            }
                        else
                            null
                    }
                }
            )
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