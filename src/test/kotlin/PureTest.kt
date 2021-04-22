import com.github.h0tk3y.betterParse.combinators.pure
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.parser.UnparsedRemainder
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import com.github.h0tk3y.betterParse.parser.tryParseToEnd
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PureTest {
    val a = Token("a", "a")
    val lexer = DefaultTokenizer(listOf(a))

    @Test fun testSuccessfulPure() {
        val tokens = lexer.tokenize("a")
        val result = pure(42).tryParse(tokens)
        assertEquals(42, result.toParsedOrThrow().value)
    }

    @Test fun testNotConsumesInputPure() {
        val tokens = lexer.tokenize("a")
        val result = pure(42).tryParseToEnd(tokens)
        assertTrue(result is UnparsedRemainder)
    }
}