import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TokenTest {
    val a = regexToken("a", "a")
    val b = literalToken("b", "b")
    val ignoredX = RegexToken("ignoredX", "x", true)
    val num = regexToken("-?[0-9]*(?:\\.[0-9]*)?")

    @Test
    fun expectIgnoredToken() {
        val tokens = DefaultTokenizer(listOf(a, b, ignoredX)).tokenize("xxaba")
        val result = ignoredX.tryParse(tokens, 0).toParsedOrThrow()
        assertEquals("x", result.value.text)
    }

    @Test
    fun successfulParse() {
        val tokens = DefaultTokenizer(listOf(a)).tokenize("aaa")
        val result = a.tryParse(tokens, 0) as Parsed

        assertEquals(a, result.value.type)
        assertEquals(listOf(a, a), tokens.drop(result.nextPosition).toList().map { it.type })
    }

    @Test
    fun unexpectedEof() {
        val tokens = TokenMatchesSequence(object : TokenProducer {
            override fun nextToken(): TokenMatch? = null
        }, DefaultTokenizer(listOf(a)), arrayListOf())

        val result = a.tryParseToEnd(tokens, 0)

        assertEquals(UnexpectedEof(a), result)
    }

    @Test
    fun noMatchingToken() {
        val input = "c"
        val tokens = DefaultTokenizer(listOf(a, b)).tokenize(input)
        val result = a.tryParse(tokens, 0)

        assertEquals(NoMatchingToken(TokenMatch(noneMatched, 0, input, 0, 1, 1, 1)), result)
    }

    @Test
    fun ignored() {
        val input = "xxxa"
        val tokens = DefaultTokenizer(listOf(ignoredX, a)).tokenize(input)
        val result = a.tryParse(tokens, 0)

        assertEquals(TokenMatch(a, 3, input, 3, 1, 1, 4), result.toParsedOrThrow().value)
    }

    @Test
    fun mismatched() {
        val input = "b"
        val tokens = DefaultTokenizer(listOf(a, b)).tokenize(input)
        val result = a.tryParse(tokens, 0)

        assertEquals(MismatchedToken(a, TokenMatch(b, 0, input, 0, 1, 1, 1)), result)
    }

    @Test
    fun mismatchedRegex() {
        val input = "b"
        val tokens = DefaultTokenizer(listOf(num)).tokenize(input)
        val result = num.tryParse(tokens, 0)

        assertEquals(NoMatchingToken(TokenMatch(noneMatched, 0, input, 0, 1, 1, 1)), result)
    }
}
