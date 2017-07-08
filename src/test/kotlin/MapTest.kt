
import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.asJust
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.lexer.Lexer
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import org.junit.Assert.assertEquals
import org.junit.Test

class MapTest {
    val aPlus = Token("aPlus", "a+")
    val bPlus = Token("aPlus", "b+")
    val lexer = Lexer(listOf(aPlus, bPlus))

    @Test fun testSuccessfulMap() {
        val tokens = lexer.tokenize("aaa")
        val result = aPlus.map { it.text }.tryParse(tokens)
        assertEquals("aaa", result.toParsedOrThrow().value)
    }

    @Test fun testSuccessfulUse() {
        val tokens = lexer.tokenize("abbaaa")
        val result = (aPlus and bPlus and aPlus use { t3.text + t2.text + t1.text}).tryParse(tokens)
        assertEquals("aaabba", result.toParsedOrThrow().value)
    }

    @Test fun testSuccessfulAsJust() {
        val tokens = lexer.tokenize("abbaaa")
        val result = (aPlus asJust 1).tryParse(tokens)
        assertEquals(1, result.toParsedOrThrow().value)
    }

    @Throws fun testError() {
        val tokens = lexer.tokenize("bbbb")
        val resultNonMapped = aPlus.tryParse(tokens)
        val resultMapped = aPlus.map { it.text }.tryParse(tokens)
        assertEquals(resultNonMapped, resultMapped)
    }
}