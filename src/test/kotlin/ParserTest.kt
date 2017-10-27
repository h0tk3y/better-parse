
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.parser.UnparsedRemainder
import com.github.h0tk3y.betterParse.parser.parseToEnd
import com.github.h0tk3y.betterParse.parser.tryParseToEnd
import org.junit.Assert.assertTrue
import org.junit.Test

class ParserTest {
    val a = Token("a", "a")
    val ignoredX = Token("ignoredX", "x", ignored = true)

    @Test fun ignoredUnparsed() {
        val tokens = DefaultTokenizer(listOf(ignoredX, a)).tokenize("axxx")
        a.parseToEnd(tokens) // should not throw
    }

    @Test fun unparsedReportsNoIgnoredTokens() {
        val tokens = DefaultTokenizer(listOf(ignoredX, a)).tokenize("axxxa")
        val result = a.tryParseToEnd(tokens)
        assertTrue(result is UnparsedRemainder && result.startsWith.type == a)
    }
}