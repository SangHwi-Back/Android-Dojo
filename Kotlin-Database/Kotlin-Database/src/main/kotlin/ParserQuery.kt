package org.example

/**
 * [[ 지원하게 될 SQL 예제들 ]]
 *
 * INSERT INTO users (name, age) VALUES ('Alice', 30);
 * SELECT name, age FROM users WHERE age > 25;
 * UPDATE users SET age = 31 WHERE name = 'Alice';
 * DELETE FROM users WHERE name = 'Alice';
 * SELECT * FROM users WHERE age > 18 AND status = 'active';
 * SELECT * FROM products WHERE price <= 50;
 *
 * SELECT A.name, B.order_item
 * FROM users AS A
 * INNER JOIN orders AS B
 * ON A.id = B.user_id;
 *
 * SELECT A.name, B.order_item
 * FROM users AS A
 * LEFT JOIN orders AS B
 * ON A.id = B.user_id;
 *
 * SELECT A.name, B.order_item
 * FROM users AS A
 * RIGHT JOIN orders AS B
 * ON A.id = B.user_id;
 */

class ParserQuery(private val sql: String) {
    val prettyPrinted: String
        get() = sql
    val tokens = mutableListOf<Token>()
    val keyword: Token.Keyword
        get() = tokens.first() as Token.Keyword

    init {
        setTokens()
    }

    @Throws(IllegalArgumentException::class)
    private fun setTokens() {
    }

    private fun getKeywords(sql: String): Token {
        val items = sql.trim().split(Regex("\\s+")).toMutableList()
        if (items.isEmpty())
            throw IllegalArgumentException("No Keyword Found")
        var keyword = items.removeFirst().uppercase()
        if (keyword == "INSERT" || keyword == "DELETE")
            keyword += " ${items.removeFirst().uppercase()}"
        return when (keyword) {
            "SELECT" -> Token.Keyword(SQLKeyword.SELECT)
            "UPDATE" -> Token.Keyword(SQLKeyword.UPDATE)
            "DELETE" -> Token.Keyword(SQLKeyword.DELETE)
            "INSERT" -> Token.Keyword(SQLKeyword.INSERT)
            else -> throw IllegalArgumentException("Unknown keyword")
        }
    }

    private fun getIdentifiers(keyword: Token.Keyword, sql: String): List<Token> {
        var sql = removeKeyword(sql).trim()
        return when (keyword.value) {
            SQLKeyword.SELECT -> {
                sql = sql.removeRange(sql.indexOf("from", 0, true), sql.lastIndex)
            }
            SQLKeyword.UPDATE -> {
                sql = sql
                    .removeRange(0, sql.indexOf("set", 0, true) + 2)
                    .removeRange(sql.indexOf("where", 0, true), sql.lastIndex)
            }
            SQLKeyword.DELETE -> {}
            SQLKeyword.INSERT -> {
                sql.removeRange(0, sql.indexOf("("))
                    .removeRange(sql.indexOf(")"), sql.lastIndex)
            }
        }.let {
            if (keyword.value == SQLKeyword.DELETE)
                return emptyList()
            sql.trim().split(Regex("\\s+")).toMutableList().map {
                Token.Identifier(it.trim().removeSuffix(","))
            }
        }
    }

    private fun getTargetTable(keyword: Token.Keyword, sql: String): String? = when (keyword.value) {
        SQLKeyword.SELECT, SQLKeyword.DELETE -> {
            sql.trim()
                .removeRange(0, sql.indexOf("from", 0, true)+3)
                .trim()
                .split(Regex("\\s+"))
                .firstOrNull()
        }
        SQLKeyword.UPDATE, SQLKeyword.INSERT -> {
            removeKeyword(sql).trim().split(Regex("\\s+")).firstOrNull()
        }
    }
}

// "SELECT name FROM users"
// → [SELECT, name, FROM, users] 이런 토큰 리스트로 변환
sealed interface Token {
    data class Keyword(val value: SQLKeyword) : Token
    data class Identifier(val value: String) : Token
    data class Operator(val value: String) : Token
}

enum class SQLKeyword {
    SELECT, INSERT, UPDATE, DELETE;

}

fun containsKeyword(sql: String): Boolean = sql.trim().split(Regex("\\s+")).let {
    if (it.isEmpty())
        return false
    else {
        when (it.first().uppercase()) {
            "SELECT", "UPDATE" -> return true
            "INSERT" -> it.getOrNull(1)?.uppercase() == "INTO"
            "DELETE" -> it.getOrNull(1)?.uppercase() == "FROM"
            else -> return false
        }
    }
}
fun removeKeyword(sql: String): String {
    if (containsKeyword(sql)) {
        sql.trim().split(Regex("\\s+")).toMutableList().let {
            return when (it.first().uppercase()) {
                "SELECT" ->
                    sql.removeRange(0, "select".length - 1)

                "UPDATE" ->
                    sql.removeRange(0, "select".length - 1)

                "INSERT" ->
                    sql.removeRange(0, sql.indexOf("into", 0, true) + 3)

                "DELETE" ->
                    sql.removeRange(0, sql.indexOf("from", 0, true) + 3)

                else ->
                    sql
            }
        }
    }
    return sql
}