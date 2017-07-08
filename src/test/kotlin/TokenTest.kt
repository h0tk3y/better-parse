
import com.github.h0tk3y.betterParse.lexer.Lexer
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.noneMatched
import com.github.h0tk3y.betterParse.parser.*
import org.junit.Assert.assertEquals
import org.junit.Test

class TokenTest {
    val a = Token("a", "a")
    val b = Token("b", "b")
    val ignoredX = Token("ignoredX", "x", ignored = true)

    @Test fun successfulParse() {
        val tokens = Lexer(listOf(a)).tokenize("aaa")
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
        val tokens = Lexer(listOf(a, b)).tokenize("c")
        val result = a.tryParse(tokens)

        assertEquals(NoMatchingToken(TokenMatch(noneMatched, "c", 0, 1, 1)), result)
    }

    @Test fun ignored() {
        val tokens = Lexer(listOf(ignoredX, a)).tokenize("xxxa")
        val result = a.tryParse(tokens)

        assertEquals(TokenMatch(a, "a", 3, 1, 4), result.toParsedOrThrow().value)
    }

    @Test fun mismatched() {
        val tokens = Lexer(listOf(a, b)).tokenize("b")
        val result = a.tryParse(tokens)

        assertEquals(MismatchedToken(a, TokenMatch(b, "b", 0, 1, 1)), result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun wrongLexer() {
        val tokens = Lexer(listOf(a, ignoredX)).tokenize("axax")
        b.tryParse(tokens)
    }
}