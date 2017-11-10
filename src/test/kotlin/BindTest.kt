import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import org.junit.Assert.assertEquals
import org.junit.Test

class BindTest {
    val a_or_b = Token("a_or_b", "[ab]")
    val b_and_a = Token("b_and_a", "ba")
    val c = Token("c", "c")
    val lexer = DefaultTokenizer(listOf(b_and_a, a_or_b, c))

    @Test fun testSuccessfulBind() {
        val tokens = lexer.tokenize("aba")
        val result = a_or_b.bind {
            when(it.text) {
                "a" -> b_and_a
                "b" -> a_or_b
                else -> c
            }
        }.tryParse(tokens)
        assertEquals("ba", result.toParsedOrThrow().value.text)
    }

    @Test fun testSuccessfulBindUse() {
        val tokens = lexer.tokenize("baccba")
        val result = (b_and_a useBind {
            when(text) {
                "ba" -> c
                "a", "b"  -> b_and_a
                else -> a_or_b
            }
        }).tryParse(tokens)
        assertEquals("c", result.toParsedOrThrow().value.text)
    }

    @Test fun testBindPure() {
        val tokens = lexer.tokenize("ba")
        val result = b_and_a.tryParse(tokens)
        val resultBindPure = b_and_a.bind { pure(it) }.tryParse(tokens)
        assertEquals("ba", result.toParsedOrThrow().value.text)
        assertEquals("ba", resultBindPure.toParsedOrThrow().value.text)
    }

    @Throws fun testError() {
        val tokens = lexer.tokenize("bbbb")
        val resultNonBinded = b_and_a.tryParse(tokens)
        val resultBinded = b_and_a.bind { pure(it.text) }.tryParse(tokens)
        assertEquals(resultNonBinded, resultBinded)
    }
}