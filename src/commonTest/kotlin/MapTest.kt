
import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.asJust
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import kotlin.test.Test
import kotlin.test.assertEquals

class MapTest {
    val aPlus = Token("aPlus", "a+")
    val bPlus = Token("aPlus", "b+")
    val lexer = DefaultTokenizer(listOf(aPlus, bPlus))

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
}