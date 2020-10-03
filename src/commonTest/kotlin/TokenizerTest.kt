import com.github.h0tk3y.betterParse.lexer.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TokenizerTest {
    @Test
    fun simpleInput() {
        val aPlus = regexToken("aPlus", "a+", false)
        val bPlus = regexToken("bPlus", "b+", false)
        val justX = literalToken("justX", "x", false)
        val lexer = DefaultTokenizer(listOf(aPlus, bPlus, justX))

        val input = "aaaxxxbbbaaa"
        val types = lexer.tokenize(input).toList().map { it.type }

        assertEquals(listOf(aPlus, justX, justX, justX, bPlus, aPlus), types)
    }

    @Test
    fun position() {
        val aPlus = RegexToken("aPlus", "a+")
        val bPlus = RegexToken("bPlus", "b+")
        val lexer = DefaultTokenizer(listOf(aPlus, bPlus))

        val input = "abaaabbbaaaabbbb"
        val positions = lexer.tokenize(input).toList().map { it.offset }

        assertEquals(listOf(0, 1, 2, 5, 8, 12), positions)
    }

    @Test
    fun rowAndColumn() {
        val aPlus = regexToken("aPlus", "a+")
        val bPlus = regexToken("bPlus", "b+")
        val br = literalToken("break", "\n")
        val lexer = DefaultTokenizer(listOf(aPlus, bPlus, br))

        val input = """
            aaa
            bbb
            ab
            """.trimIndent()

        val rowCols = lexer.tokenize(input).toList().map { it.row to it.column }

        assertEquals(
            listOf(
                1 to 1, 1 to 4,
                2 to 1, 2 to 4,
                3 to 1, 3 to 2
            ), rowCols
        )
    }

    @Test
    fun mismatchedToken() {
        val a = RegexToken("a", "a")
        val b = RegexToken("b", "b")
        val lexer = DefaultTokenizer(listOf(a, b))

        val input = "aabbxxaa"

        val result = lexer.tokenize(input).toList()

        assertEquals(5, result.size)
        assertEquals(noneMatched, result.last().type)
        assertEquals("xxaa", result.last().text)
        assertEquals(4, result.last().offset)
    }

    @Test
    fun priority() {
        val a = regexToken("a", "a")
        val aa = literalToken("aa", "aa")

        val input = "aaaaaa"

        val lexAFirst = DefaultTokenizer(listOf(a, aa))
        val resultAFirst = lexAFirst.tokenize(input).toList().map { it.type }
        assertEquals(6, resultAFirst.size)
        assertEquals(a, resultAFirst.distinct().single())

        val lexAaFirst = DefaultTokenizer(listOf(aa, a))
        val resultAaFirst = lexAaFirst.tokenize(input).toList().map { it.type }
        assertEquals(3, resultAaFirst.size)
        assertEquals(aa, resultAaFirst.distinct().single())
    }

    @Test
    fun tokensWithGroups() {
        val a = regexToken("a(b)c")
        val b = regexToken("d(e)f")
        val c = regexToken("g(h)i")
        val input = "abcdefghi"
        val lex = DefaultTokenizer(listOf(a, b, c))
        val result = lex.tokenize(input)

        assertEquals(listOf(a, b, c), result.toList().map { it.type })
    }

    @Test
    fun issue28() {
        val a = regexToken("a+".toRegex())
        val b = regexToken("b+".toRegex())
        val lex = DefaultTokenizer(listOf(a, b))
        val input = "abab"
        assertEquals(listOf(a, b, a, b), lex.tokenize(input).toList().map(TokenMatch::type))
    }
}
