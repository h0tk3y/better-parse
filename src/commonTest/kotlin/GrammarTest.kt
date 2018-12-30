import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import kotlin.test.*

class GrammarTest {
    @Test
    fun simpleParse() {
        val digits = "0123456789"
        val g = object : Grammar<Int>() {
            val n by token {
                var length = 0
                while (length < it.length && it[length] in digits)
                    length++
                length
            }
            val s by regexToken("\\-|\\+")
            val ws by regexToken("\\s+", ignore = true)

            override val rootParser: Parser<Int> = separated(n use { text.toInt() }, s use { text }).map {
                it.reduce { a, s, b ->
                    if (s == "+") a + b else a - b
                }
            }
        }

        val result = g.parseToEnd("1 + 2 + 3 + 4 - 11")
        assertEquals(-1, result)
    }
}