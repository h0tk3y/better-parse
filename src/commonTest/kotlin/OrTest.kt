import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.parser.*
import kotlin.test.*

class OrTest : Grammar<Nothing>() {
    override val rootParser: Parser<Nothing> get() = throw NoSuchElementException()

    val a by token("a")
    val b by token("b")

    @Test fun aOrB() {
        val tokens = tokenizer.tokenize("abababa")
        val abOrA = zeroOrMore((a and b use { t2 }) or a) use { map { it.type } }
        val result = abOrA.parseToEnd(tokens)

        assertEquals(listOf(b, b, b, a), result)
    }

    @Test fun alternativesError() {
        val tokens = tokenizer.tokenize("ab")
        val parser = (a and a) or (a and b and a)
        val result = parser.tryParse(tokens) as AlternativesFailure

        assertTrue(result.errors[0] is MismatchedToken)
        assertTrue(result.errors[1] is UnexpectedEof)
    }
}