
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.utils.*
import kotlin.test.*

class OptionalTest : Grammar<Nothing>() {
    override val rootParser: Parser<Nothing> get() = throw NoSuchElementException()

    val a by regexToken("a")
    val b by regexToken("b")

    @Test fun successful() {
        val tokens = tokenizer.tokenize("abab")
        val result = optional(a and b and a and b).tryParse(tokens,0)
        assertTrue(result.toParsedOrThrow().value is Tuple4)
    }

    @Test fun unsuccessful() {
        val tokens = tokenizer.tokenize("abab")
        val result = optional(b and a and b and a).tryParse(tokens,0)
        assertNull(result.toParsedOrThrow().value)
    }
}