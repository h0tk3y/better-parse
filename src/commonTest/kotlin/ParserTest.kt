import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.RegexToken
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.parser.UnparsedRemainder
import com.github.h0tk3y.betterParse.parser.parseToEnd
import com.github.h0tk3y.betterParse.parser.tryParseToEnd
import kotlin.test.Test
import kotlin.test.assertTrue

internal class ParserTest {
    val a = literalToken("a", "a")
    val ignoredX = RegexToken("ignoredX", "x", ignored = true)

    @Test
    fun ignoredUnparsed() {
        val tokens = DefaultTokenizer(listOf(ignoredX, a)).tokenize("axxx")
        a.parseToEnd(tokens) // should not throw
    }

    @Test
    fun unparsedReportsNoIgnoredTokens() {
        val tokens = DefaultTokenizer(listOf(ignoredX, a)).tokenize("axxxa")
        val result = a.tryParseToEnd(tokens, 0)
        assertTrue(result is UnparsedRemainder && result.startsWith.type == a)
    }
}
