import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import kotlin.test.Test

class LeftRecursionTest {

    class SumGrammar : Grammar<Int>() {
        val num by regexToken("\\d+")
        val n by num.map { it.text.toInt() }

        val plus by literalToken("+")

        val sum: Parser<Int> = (parser(this::sum) * -plus * n).map { (t1, t2) -> t1 + t2 }.or(n)

        override
        val rootParser: Parser<Int>
            get() = sum
    }

    @Test
    fun testSimpleLr() {
        val grammar = SumGrammar()

        val input = "1+2+3+4+5+6"
        println(grammar.tryParseToEnd(input))
    }
}