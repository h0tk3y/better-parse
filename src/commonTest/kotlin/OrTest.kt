import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OrTest : Grammar<Nothing>() {
    override val rootParser: Parser<Nothing> get() = throw NoSuchElementException()

    val a by regexToken("a")
    val b by regexToken("b")

    @Test
    fun aOrB() {
        val tokens = tokenizer.tokenize("abababa")
        val abOrA = zeroOrMore((a and b use { t2 }) or a) use { map { it.type } }
        val result = abOrA.parseToEnd(tokens)

        assertEquals(listOf(b, b, b, a), result)
    }

    @Test
    fun alternativesError() {
        val tokens = tokenizer.tokenize("ab")
        val parser = (a and a) or (a and b and a)
        val result = parser.tryParse(tokens, 0) as AlternativesFailure

        assertTrue(result.errors[0] is MismatchedToken)
        assertTrue(result.errors[1] is UnexpectedEof)
    }
}