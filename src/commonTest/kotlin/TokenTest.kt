import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import kotlin.test.*

class TokenTest {
    val a = tokenRegex("a", "a")
    val b = tokenText("b", "b")
    val ignoredX = TokenRegex("ignoredX", "x", ignored = true)
    val num = tokenRegex("-?[0-9]*(?:\\.[0-9]*)?")

    @Test
    fun successfulParse() {
        val tokens = DefaultTokenizer(listOf(a)).tokenize("aaa")
        val result = a.tryParse(tokens, 0) as Parsed

        assertEquals(a, result.value.type)
        assertEquals(listOf(a, a), tokens.toList(result.nextPosition).map { it.type })
    }

/*
    @Test fun unexpectedEof() {
        val tokens = sequenceOf<TokenMatch>()
        val result = a.tryParse(tokens)

        assertEquals(UnexpectedEof(a), result)
    }
*/

    @Test
    fun noMatchingToken() {
        val input = "c"
        val tokens = DefaultTokenizer(listOf(a, b)).tokenize(input)
        val result = a.tryParse(tokens, 0)

        assertEquals(NoMatchingToken(TokenMatch(noneMatched, input, 0, 1, 1, 1)), result)
    }

    @Test
    fun ignored() {
        val input = "xxxa"
        val tokens = DefaultTokenizer(listOf(ignoredX, a)).tokenize(input)
        val result = a.tryParse(tokens, 0)

        assertEquals(TokenMatch(a, input, 3, 1, 1, 4), result.toParsedOrThrow().value)
    }

    @Test
    fun mismatched() {
        val input = "b"
        val tokens = DefaultTokenizer(listOf(a, b)).tokenize(input)
        val result = a.tryParse(tokens, 0)

        assertEquals(MismatchedToken(a, TokenMatch(b, input, 0, 1, 1, 1)), result)
    }

    @Test
    fun mismatchedRegex() {
        val input = "b"
        val tokens = DefaultTokenizer(listOf(num)).tokenize(input)
        val result = num.tryParse(tokens, 0)

        assertEquals(NoMatchingToken(TokenMatch(noneMatched, input, 0, 1, 1, 1)), result)
    }

    @Test
    fun wrongLexer() {
        assertFailsWith<IllegalArgumentException> {
            val tokens = DefaultTokenizer(listOf(a, ignoredX)).tokenize("axax")
            b.tryParse(tokens,0)
        }
    }
}