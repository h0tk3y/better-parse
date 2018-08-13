
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.components
import org.junit.Assert
import org.junit.Test
import java.util.regex.Pattern
import java.util.regex.Pattern.compile
import kotlin.text.RegexOption.*

class FlagsCompatibilityTest {
    @Test
    fun testJavaPatternFlags() {
        val patternsGrammar = object : Grammar<String>() {
            val caseInsensitive by token(compile("abc", Pattern.CASE_INSENSITIVE))

            val comments by token(
                compile("""
                    d # some comment
                    e # some comment
                    f # some comment
                """.trimIndent(), Pattern.COMMENTS
                )
            )

            val dotall by token(compile("eol.*?dotall", Pattern.DOTALL))

            val literal by token(compile(".*.*.*", Pattern.LITERAL))

            val multiline by token(compile("eol$\n^multiline", Pattern.MULTILINE))

            val unicodeCase by token(compile("абвгд", Pattern.UNICODE_CASE or Pattern.CASE_INSENSITIVE))

            val unixLines by token(compile(". x", Pattern.UNIX_LINES))

            override val rootParser: Parser<String>
                get() = (caseInsensitive * comments * dotall * literal * multiline * unicodeCase * unixLines).use {
                    components.joinToString("") { it.text }
                }
        }

        val input = """
            AbCdefeol
            dotall.*.*.*eol
            multilineАбВгД
        """.trimIndent() + "\r x"

        val result = patternsGrammar.parseToEnd(input)
        Assert.assertEquals(input, result)
    }

    @Test
    fun testKotlinRegexFlags() {
        val regexesGrammar = object : Grammar<String>() {
            val caseInsensitive by token("abc".toRegex(IGNORE_CASE))

            val comments by token(
                """
                    d # some comment
                    e # some comment
                    f # some comment
                """.trimIndent().toRegex(RegexOption.COMMENTS)
            )

            val dotall by token("eol.*?dotall".toRegex(DOT_MATCHES_ALL))

            val literal by token(".*.*.*".toRegex(RegexOption.LITERAL))

            val multiline by token("eol$\n^multiline".toRegex(RegexOption.MULTILINE))

            val unixLines by token(". x".toRegex(UNIX_LINES))

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
        Assert.assertEquals(input, result)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testCanonEqUnsupported() {
        val badGrammar = object : Grammar<Unit>() {
            val badToken by token(compile("abc", Pattern.CANON_EQ))

            override val rootParser: Parser<Unit>
                get() = badToken use { Unit }
        }

        println(badGrammar.parseToEnd("abc"))
    }
}