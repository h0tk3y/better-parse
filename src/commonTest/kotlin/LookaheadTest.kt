import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import kotlin.test.*

class LookaheadTest {
    @Test fun abAndNotD() {
        val grammar = object : Grammar<String>() {
            private val a by literalToken("a")
            private val b by literalToken("b")
            private val d by literalToken("d")
            private val trailer by regexToken(".")

            val ap by a.use { input[offset] }
            val bp by b use { input[offset] }
            val dp by d use { input[offset] }
            val tp by trailer use { input[offset] }

            val abAndNotD by ap and bp and not(dp) and zeroOrMore(tp)

            override val rootParser = abAndNotD.map {
                // TODO can we hide the Unit?
                it.t1.toString() + it.t2.toString() + it.t4.joinToString("")
            }
        }

        assertEquals(LookaheadFoundInNegativeLookahead, grammar.tryParseToEnd("abd"))
        assertEquals(ParsedValue("abc", 3), grammar.tryParseToEnd("abc"))
        assertEquals(ParsedValue("abcc", 4), grammar.tryParseToEnd("abcc"))
    }

    @Test
    fun testNegativeLookahead() {
        val grammar = object : Grammar<Int>() {
            val digits by regexToken("[0-9]+")
            val dot by literalToken(".")
            val semi by literalToken(";")

            override val rootParser: Parser<Int>
                get() = (digits * not(dot * digits) * -semi).use { t1.text.toInt() }
        }

        assertEquals(LookaheadFoundInNegativeLookahead, grammar.tryParseToEnd("1.0;"))
        assertEquals(ParsedValue(1, 2), grammar.tryParseToEnd("1;"))
    }

    @Test
    fun testNegativeLookaheadBasic() {
        val grammar = object : Grammar<String>() {
            val a by literalToken("A")
            val b by literalToken("B")
            val c by literalToken("C")

            val aParser by a use { input[offset] }
            val bParser by b use { input[offset] }
            val cParser by c use { input[offset] }

            override val rootParser = (aParser and not(bParser) and zeroOrMore(cParser)).map {
                it.t1.toString() + it.t3.joinToString("")
            }
        }

        assertEquals(ParsedValue("A", 1), grammar.tryParseToEnd("A"))
        assertEquals(LookaheadFoundInNegativeLookahead, grammar.tryParseToEnd("AB"))
        assertEquals(LookaheadFoundInNegativeLookahead, grammar.tryParseToEnd("ABC"))
        assertEquals(ParsedValue("ACC", 3), grammar.tryParseToEnd("ACC"))
        assertEquals(ParsedValue("ACCC", 4), grammar.tryParseToEnd("ACCC"))
    }

    @Test
    fun testPositiveLookaheadBasic() {
        val grammar = object : Grammar<String>() {
            val a by literalToken("A")
            val b by literalToken("B")

            val aParser by a use { input[offset] }
            val bParser by b use { input[offset] }

            override val rootParser = (aParser and pos(bParser)).map {
                it.t1.toString()
            }
        }

        assertEquals(ParsedValue("A", 1), grammar.tryParse(grammar.tokenizer.tokenize("AB"), 0))
        assertTrue(grammar.tryParseToEnd("A") is ErrorResult) // Didn't find "B"
        assertTrue(grammar.tryParseToEnd("AC") is ErrorResult) // Didn't find "B"
    }

    @Test
    fun testNegativeLookahead2() {
        // Ported from https://github.com/kevinmehall/rust-peg/blob/5269c2e91faa34c2696a10f6edc73a4a60491ed8/tests/run-pass/pos_neg_assert.rs#L4-L5
        val grammar = object : Grammar<List<Char>>() {
            val vowel by MultiCharToken("vowel", "aeiou".toCharArray().toSet())
            val letter by MultiCharToken("letter", ('a'..'z').toSet())

            val vowelParser by vowel use { input[offset] }
            val letterParser by letter use { input[offset] }

            val consonants by oneOrMore(skip(not(vowelParser)) * letterParser)

            override val rootParser = consonants
        }

        assertEquals(ParsedValue(listOf('q','w','r','t','y'), 5), grammar.tryParseToEnd("qwrty"))
        val res = grammar.tryParseToEnd("rust")
        assertTrue(res is UnparsedRemainder) // TODO the Lookahead failure is swallowed by the OneOrMore
        assertTrue(res.startsWith.offset == 1)
    }

    @Test
    fun testPositiveLookahead() {
        // ported from https://github.com/kevinmehall/rust-peg/blob/5269c2e91faa34c2696a10f6edc73a4a60491ed8/tests/run-pass/pos_neg_assert.rs#L7
        val grammar = object : Grammar<Unit>() {
            val abc by MultiCharToken("abc", "abc".toSet(), false)

            val abcP by zeroOrMore(abc) use { joinToString("") }

            val consonants by pos(abcP)

            override val rootParser = consonants
        }

        val res = grammar.tryParseToEnd("abc")
        println(res)
    }

}
