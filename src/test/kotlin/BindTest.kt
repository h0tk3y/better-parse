
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import org.junit.Assert.assertEquals
import org.junit.Test

class BindTest {
    val aPlus = Token("aPlus", "a+")
    val bPlus = Token("bPlus", "b+")
    val a     = Token("a", "a")
    val b     = Token("b", "b")
    val lexer = DefaultTokenizer(listOf(aPlus, bPlus))

    @Test fun testSuccessfulMap() {
        val tokens = lexer.tokenize("aaabaaa")
        val result = aPlus.bind { bPlus and Token("testSuccessfulMap-bind", it.text) }.tryParse(tokens)
        assertEquals("baaa", result.toParsedOrThrow().value)
    }

    @Test fun testSuccessfulBindUse() {
        val tokens = lexer.tokenize("abbaaa")
        val result = (aPlus and bPlus useBind { (t1.text.length + t2.text.length) times a }).tryParse(tokens)
        assertEquals("aaa", result.toParsedOrThrow().value)
    }

    @Throws fun testError() {
        val tokens = lexer.tokenize("bbbb")
        val resultNonBinded = aPlus.tryParse(tokens)
        val resultBinded = aPlus.bind { Token("testError-bind", it.text) }.tryParse(tokens)
        assertEquals(resultNonBinded, resultBinded)
    }
}