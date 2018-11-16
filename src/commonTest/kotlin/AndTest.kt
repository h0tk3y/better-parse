
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.utils.*
import kotlin.test.*

class AndTest : Grammar<Nothing>() {
    override val rootParser: Parser<Nothing> get() = throw NoSuchElementException()

    val a by tokenRegex("a")
    val b by tokenRegex("b")

    @Test fun simpleAnd() {
        val tokens = tokenizer.tokenize("aba")
        val parser = a and b and a use { components.map { it.type } }
        val result = parser.parseToEnd(tokens)

        assertEquals(listOf(a, b, a), result)
    }

    @Test fun skip() {
        val tokens = tokenizer.tokenize("abab")
        val parserA = a and skip(b) and a and skip(b) use { components.map { it.type } }
        val parserB = skip(a) and b and skip(a) and b use { components.map { it.type } }

        assertEquals(listOf(a, a), parserA.parseToEnd(tokens))
        assertEquals(listOf(b, b), parserB.parseToEnd(tokens))
    }

    @Test fun leftmostSeveralSkips() {
        val tokens = tokenizer.tokenize("ababab")
        val parser = -a * -b * a * -b * -a * b use { t1.type to t2.type }
        val result = parser.parseToEnd(tokens)

        assertEquals(a to b, result)
    }

    @Test fun singleParserInSkipChain() {
        val tokens = tokenizer.tokenize("ababa")
        val parser = -a * -b * a * -b * -a use { position }
        val result = parser.parseToEnd(tokens)

        assertEquals(2, result)
    }

    @Test fun longAndOperatorChain() {
        val tokens = tokenizer.tokenize("aaabbb")
        val parser = a * a * a * b * b * b use { listOf(t6, t5, t4, t3, t2, t1).map { it.type } }
        val result = parser.parseToEnd(tokens)

        assertEquals(listOf(b, b, b, a, a, a), result)
    }
}