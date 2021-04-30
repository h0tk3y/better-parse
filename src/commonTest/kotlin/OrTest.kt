import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import kotlin.test.*

class OrTest {

    val a by regexToken("a")
    val aa by regexToken("aa")
    val b by regexToken("b")

    @Test fun aOrB() {
        assertEquals(
            listOf(b, b, b, a),
            grammar(zeroOrMore((a and b use { t2 }) or a) use { map { it.type } })
                .parseToEnd("abababa"))
    }

    @Test fun alternativesError() {
        val result = grammar((a and a) or (a and b and a)).tryParseToEnd("ab") as AlternativesFailure

        assertTrue(result.errors[0] is MismatchedToken)
        assertTrue(result.errors[1] is UnexpectedEof)
    }

    @Test fun aOrAa() {
        // aa defined first
        assertEquals(
            listOf("aa"),
            grammar(oneOrMore(aa or a) use { map { it.text} }).parseToEnd("aa")
        )

        // a defined first
        assertEquals(
            listOf("a", "a"),
            grammar(oneOrMore(a or aa) use { map { it.text} }).parseToEnd("aa")
        )

        /*
         * a is first in the declared tokens, but aa first in the parser. For backwards
         * compatibility, the order of declared tokens is honored.
         */
        val grammar = object : Grammar<List<String>>() {
            val a by regexToken("a")
            val aa by regexToken("aa")
            override val rootParser: Parser<List<String>> by oneOrMore(aa or a) use { map { it.text } }
        }

        assertEquals(listOf("a", "a"), grammar.parseToEnd("aa"))
    }
}
