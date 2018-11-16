
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import kotlin.test.*

class TokenTest {
    val a = tokenRegex("a", "a")
    val b = tokenText("b", "b")
    val ignoredX = TokenRegex("ignoredX", "x", ignored = true)
    val num = tokenRegex("-?[0-9]*(?:\\.[0-9]*)?")

    @Test fun successfulParse() {
        val tokens = DefaultTokenizer(listOf(a)).tokenize("aaa")
        val result = a.tryParse(tokens) as Parsed

        assertEquals(a, result.value.type)
        assertEquals(listOf(a, a), result.remainder.toList().map { it.type })
    }

    @Test fun unexpectedEof() {
        val tokens = sequenceOf<TokenMatch>()
        val result = a.tryParse(tokens)

        assertEquals(UnexpectedEof(a), result)
    }

    @Test fun noMatchingToken() {
        val tokens = DefaultTokenizer(listOf(a, b)).tokenize("c")
        val result = a.tryParse(tokens)

        assertEquals(NoMatchingToken(TokenMatch(noneMatched, "c", 0, 1, 1)), result)
    }

    @Test fun ignored() {
        val tokens = DefaultTokenizer(listOf(ignoredX, a)).tokenize("xxxa")
        val result = a.tryParse(tokens)

        assertEquals(TokenMatch(a, "a", 3, 1, 4), result.toParsedOrThrow().value)
    }

    @Test fun mismatched() {
        val tokens = DefaultTokenizer(listOf(a, b)).tokenize("b")
        val result = a.tryParse(tokens)

        assertEquals(MismatchedToken(a, TokenMatch(b, "b", 0, 1, 1)), result)
    }
    
    @Test fun mismatchedRegex() {
        val tokens = DefaultTokenizer(listOf(num)).tokenize("b")
        val result = num.tryParse(tokens)

        assertEquals(NoMatchingToken(TokenMatch(noneMatched, "b", 0, 1, 1)), result)
    }

    @Test
    fun wrongLexer() {
        assertFailsWith<IllegalArgumentException> {
            val tokens = DefaultTokenizer(listOf(a, ignoredX)).tokenize("axax")
            b.tryParse(tokens)
        }
    }
}