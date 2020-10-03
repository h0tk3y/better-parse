import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.asJust
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.RegexToken
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MapTest {
    val aPlus = RegexToken("aPlus", "a+")
    val bPlus = RegexToken("aPlus", "b+")
    val lexer = DefaultTokenizer(listOf(aPlus, bPlus))

    @Test
    fun testSuccessfulMap() {
        val tokens = lexer.tokenize("aaa")
        val result = aPlus.map { it.text }.tryParse(tokens, 0)
        assertEquals("aaa", result.toParsedOrThrow().value)
    }

    @Test
    fun testSuccessfulUse() {
        val tokens = lexer.tokenize("abbaaa")
        val result = (aPlus and bPlus and aPlus use { t3.text + t2.text + t1.text }).tryParse(tokens, 0)
        assertEquals("aaabba", result.toParsedOrThrow().value)
    }

    @Test
    fun testSuccessfulAsJust() {
        val tokens = lexer.tokenize("abbaaa")
        val result = (aPlus asJust 1).tryParse(tokens, 0)
        assertEquals(1, result.toParsedOrThrow().value)
    }
}
