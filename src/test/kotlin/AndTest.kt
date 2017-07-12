import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import com.github.h0tk3y.betterParse.utils.components
import org.junit.Assert
import org.junit.Test

class AndTest : Grammar<Nothing>() {
    override val rootParser: Parser<Nothing> get() = throw NoSuchElementException()

    val a by token("a")
    val b by token("b")

    @Test fun simpleAnd() {
        val tokens = lexer.tokenize("aba")
        val parser = a and b and a use { components.map { it.type } }
        val result = parser.parseToEnd(tokens)

        Assert.assertEquals(listOf(a, b, a), result)
    }

    @Test fun skip() {
        val tokens = lexer.tokenize("abab")
        val parserA = a and skip(b) and a and skip(b) use { components.map { it.type } }
        val parserB = skip(a) and b and skip(a) and b use { components.map { it.type } }

        Assert.assertEquals(listOf(a, a), parserA.parseToEnd(tokens))
        Assert.assertEquals(listOf(b, b), parserB.parseToEnd(tokens))
    }

    @Test fun leftmostSeveralSkips() {
        val tokens = lexer.tokenize("ababab")
        val parser = -a * -b * a * -b * -a * b use { t1.type to t2.type }
        val result = parser.parseToEnd(tokens)

        Assert.assertEquals(a to b, result)
    }

    @Test fun singleParserInSkipChain() {
        val tokens = lexer.tokenize("ababa")
        val parser = -a * -b * a * -b * -a use { position }
        val result = parser.parseToEnd(tokens)

        Assert.assertEquals(2, result)
    }

    @Test fun longAndOperatorChain() {
        val tokens = lexer.tokenize("aaabbb")
        val parser = a * a * a * b * b * b use { listOf(t6, t5, t4, t3, t2, t1).map { it.type } }
        val result = parser.parseToEnd(tokens)

        Assert.assertEquals(listOf(b, b, b, a, a, a), result)
    }
}