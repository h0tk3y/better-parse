import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.parser.*
import org.junit.Assert
import org.junit.Test

class OrTest : Grammar<Nothing>() {
    override val rootParser: Parser<Nothing> get() = throw NoSuchElementException()

    val a by token("a")
    val b by token("b")

    @Test fun aOrB() {
        val tokens = lexer.tokenize("abababa")
        val abOrA = zeroOrMore((a and b use { t2 }) or a) use { map { it.type } }
        val result = abOrA.parseToEnd(tokens)

        Assert.assertEquals(listOf(b, b, b, a), result)
    }

    @Test fun alternativesError() {
        val tokens = lexer.tokenize("ab")
        val parser = (a and a) or (a and b and a)
        val result = parser.tryParse(tokens) as AlternativesFailure

        Assert.assertTrue(result.errors[0] is MismatchedToken)
        Assert.assertTrue(result.errors[1] is UnexpectedEof)
    }
}