
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.utils.*
import kotlin.test.*
import kotlin.text.RegexOption.*

class FlagsCompatibilityTest {
    @Test
    fun testJavaPatternFlags() {
        val patternsGrammar = object : Grammar<String>() {
            val caseInsensitive by regexToken(Regex("abc", IGNORE_CASE))

            val comments by regexToken(
                Regex(
                    """
                    d # some comment
                    e # some comment
                    f # some comment
                """.trimIndent(),
                    COMMENTS
                )
            )

            val dotall by regexToken(Regex("eol.*?dotall", DOT_MATCHES_ALL))

            val literal by regexToken(Regex(".*.*.*", LITERAL))

            val multiline by regexToken(Regex("eol$\n^multiline", MULTILINE))

            val unixLines by regexToken(Regex(". x", UNIX_LINES))

            override val rootParser: Parser<String>
                get() = (caseInsensitive * comments * dotall * literal * multiline /** unicodeCase*/ * unixLines).use {
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

    @Test(expected = UnsupportedOperationException::class)
    fun testCanonEqUnsupported() {
        val badGrammar = object : Grammar<Unit>() {
            val badToken by regexToken(Regex("abc", CANON_EQ))

            override val rootParser: Parser<Unit>
                get() = badToken use { Unit }
        }

        println(badGrammar.parseToEnd("abc"))
    }
}