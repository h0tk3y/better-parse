
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import kotlin.test.*

class ParserTest {
    val a = tokenText("a", "a")
    val ignoredX = TokenRegex("ignoredX", "x", ignored = true)

    @Test fun ignoredUnparsed() {
        val tokens = DefaultTokenizer(listOf(ignoredX, a)).tokenize("axxx")
        a.parseToEnd(tokens) // should not throw
    }

    @Test fun unparsedReportsNoIgnoredTokens() {
        val tokens = DefaultTokenizer(listOf(ignoredX, a)).tokenize("axxxa")
        val result = a.tryParseToEnd(tokens,0)
        assertTrue(result is UnparsedRemainder && result.startsWith.type == a)
    }
}