import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.components
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.text.RegexOption.*

internal class FlagsCompatibilityTest {
    @Test
    fun testJavaPatternFlags() {
        val patternsGrammar = object : Grammar<String>() {
            val caseInsensitive by regexToken(Regex("abc", IGNORE_CASE))

            val comments by regexToken(
                """
                    d # some comment
                    e # some comment
                    f # some comment
                """.trimIndent().toRegex(COMMENTS)
            )

            val dotall by regexToken(Regex("eol.*?dotall", DOT_MATCHES_ALL))
            val literal by regexToken(Regex(".*.*.*", LITERAL))
            val multiline by regexToken(Regex("eol$\n^multiline", MULTILINE))
            val unixLines by regexToken(Regex(". x", UNIX_LINES))

            override val rootParser: Parser<String>
                get() = (caseInsensitive * comments * dotall * literal * multiline
                        /** unicodeCase*/
                        * unixLines).use {
                    components.joinToString("") { it.text }
                }
        }

        val input = """
            AbCdefeol
            dotall.*.*.*eol
            multiline
        """.trimIndent() + "\r x"

        val result = patternsGrammar.parseToEnd(input)
        assertEquals(input, result)
    }

    @Test
    fun testKotlinRegexFlags() {
        val regexesGrammar = object : Grammar<String>() {
            val caseInsensitive by regexToken("abc".toRegex(IGNORE_CASE))

            val comments by regexToken(
                """
                    d # some comment
                    e # some comment
                    f # some comment
                """.trimIndent().toRegex(COMMENTS)
            )

            val dotall by regexToken("eol.*?dotall".toRegex(DOT_MATCHES_ALL))

            val literal by regexToken(".*.*.*".toRegex(LITERAL))

            val multiline by regexToken("eol$\n^multiline".toRegex(MULTILINE))

            val unixLines by regexToken(". x".toRegex(UNIX_LINES))

            override val rootParser: Parser<String>
                get() = (caseInsensitive * comments * dotall * literal * multiline * unixLines).use {
                    components.joinToString("") { it.text }
                }
        }

        val input = """
            AbCdefeol
            dotall.*.*.*eol
            multiline
        """.trimIndent() + "\r x"

        val result = regexesGrammar.parseToEnd(input)
        assertEquals(input, result)
    }
}
