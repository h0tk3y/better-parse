
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.utils.*
import kotlin.test.*

class OptionalTest : Grammar<Nothing>() {
    override val rootParser: Parser<Nothing> get() = throw NoSuchElementException()

    val a by tokenRegex("a")
    val b by tokenRegex("b")

    @Test fun successful() {
        val tokens = tokenizer.tokenize("abab")
        val result = optional(a and b and a and b).tryParse(tokens)
        assertTrue(result.toParsedOrThrow().value is Tuple4)
    }

    @Test fun unsuccessful() {
        val tokens = tokenizer.tokenize("abab")
        val result = optional(b and a and b and a).tryParse(tokens)
        assertNull(result.toParsedOrThrow().value)
    }
}