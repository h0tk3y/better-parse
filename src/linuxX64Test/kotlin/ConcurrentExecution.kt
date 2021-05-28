import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import com.github.h0tk3y.betterParse.utils.components
import kotlin.native.concurrent.TransferMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.native.concurrent.Worker

class ConcurrentExecution {

    object TestGrammar : Grammar<Nothing>() {
        override val rootParser: Parser<Nothing> get() = throw NoSuchElementException()

        val a by regexToken("a")
        val b by regexToken("b")

        val parser = a and b and a use { components.map { it.type } }
    }

    @Test
    fun foo() {
        val worker = Worker.start()
        worker.execute(TransferMode.UNSAFE, { "aba" }) { string ->
            val tokens = TestGrammar.tokenizer.tokenize(string)
            TestGrammar.parser.parseToEnd(tokens)
        }.consume { result ->
            assertEquals(listOf(TestGrammar.a, TestGrammar.b, TestGrammar.a), result)
        }
    }
}
