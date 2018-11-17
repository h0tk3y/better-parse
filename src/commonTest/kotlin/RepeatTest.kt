
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import kotlin.test.*

class RepeatTest : Grammar<Nothing>() {
    override val rootParser: Parser<Nothing> get() = throw NoSuchElementException()

    val a by tokenRegex("a")

    @Test fun repeat() {
        val minN = 0
        val maxN = 30

        val rangeParsers =
            listOf(0..maxN to zeroOrMore(a)) +
            (1..maxN to oneOrMore(a)) +
            (minN..maxN).map { (it..it) to (it times a) } +
            (minN..maxN).map { (it..maxN) to (it timesOrMore a) } +
            (minN..maxN).flatMap { i ->
                val timesLess = (minN until i).map { j -> (j..i) to (j..i times a) }
                val timesMore = (i + 1..maxN).map { j -> (i..j) to (i..j times a) }
                timesLess + timesMore
            }

        for (n in minN..maxN) {
            val input = String(CharArray(n) { 'a' })
            val tokens = tokenizer.tokenize(input)

            for ((range, parser) in rangeParsers) {
                val result = parser.tryParseToEnd(tokens,0)

                when {
                    n in range -> assertTrue(result is Parsed)
                    n > range.last -> assertTrue(result is UnparsedRemainder)
                    n < range.first -> assertTrue(result is UnexpectedEof)
                }
            }
        }
    }
}