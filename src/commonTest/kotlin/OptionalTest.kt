import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import com.github.h0tk3y.betterParse.utils.Tuple4
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class OptionalTest : Grammar<Nothing>() {
    override val rootParser: Parser<Nothing> get() = throw NoSuchElementException()

    val a by regexToken("a")
    val b by regexToken("b")

    @Test
    fun successful() {
        val tokens = tokenizer.tokenize("abab")
        val result = optional(a and b and a and b).tryParse(tokens, 0)
        assertTrue(result.toParsedOrThrow().value is Tuple4)
    }

    @Test
    fun unsuccessful() {
        val tokens = tokenizer.tokenize("abab")
        val result = optional(b and a and b and a).tryParse(tokens, 0)
        assertNull(result.toParsedOrThrow().value)
    }
}
