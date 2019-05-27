
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.separated
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.lexer.token
import com.github.h0tk3y.betterParse.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class GrammarTest {
    @Test
    fun simpleParse() {
        val digits = "0123456789"
        val g = object : Grammar<Int>() {
            val n by token { input, from ->
                var length = 0
                while (from + length < input.length && input[from + length] in digits)
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