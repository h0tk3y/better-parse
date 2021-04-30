
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.utils.components
import kotlin.test.Test
import kotlin.test.assertEquals

class AndTest {

    val a by regexToken("a")
    val b by regexToken("b")

    @Test fun simpleAnd() {
        assertEquals(
            listOf(a, b, a),
            grammar(a and b and a use { components.map { it.type } }).parseToEnd("aba"))
    }

    @Test fun simpleAndInline() {
        assertEquals(
            listOf("a", "b"),
            grammar(regexToken("a") and regexToken("b") use { components.map { it.text } })
                .parseToEnd("ab"))
    }

    @Test fun skip() {
        assertEquals(
            listOf(a, a),
            grammar(a and skip(b) and a and skip(b) use { components.map { it.type } })
                .parseToEnd("abab"))

        assertEquals(
            listOf(b, b),
            grammar(skip(a) and b and skip(a) and b use { components.map { it.type } })
                .parseToEnd("abab"))
    }

    @Test fun leftmostSeveralSkips() {
        assertEquals(
            a to b,
            grammar(-a * -b * a * -b * -a * b use { t1.type to t2.type }).parseToEnd("ababab"))
    }

    @Test fun singleParserInSkipChain() {
        assertEquals(2, grammar(-a * -b * a * -b * -a use { offset }).parseToEnd("ababa"))
    }

    @Test fun longAndOperatorChain() {
        assertEquals(
            listOf(b, b, b, a, a, a),
            grammar(a * a * a * b * b * b use { listOf(t6, t5, t4, t3, t2, t1).map { it.type } })
                .parseToEnd("aaabbb"))
    }
}