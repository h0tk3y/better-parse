
import com.github.h0tk3y.betterParse.lexer.*
import kotlin.test.*

class TokenizerTest {
    @Test fun simpleInput() {
        val aPlus = tokenRegex("aPlus", "a+", false)
        val bPlus = tokenRegex("bPlus", "b+", false)
        val justX = tokenText("justX", "x", false)
        val lexer = DefaultTokenizer(listOf(aPlus, bPlus, justX))

        val input = "aaaxxxbbbaaa"
        val types = lexer.tokenize(input).toList().map { it.type }

        assertEquals(listOf(aPlus, justX, justX, justX, bPlus, aPlus), types)
    }

    @Test fun position() {
        val aPlus = TokenRegex("aPlus", "a+")
        val bPlus = TokenRegex("bPlus", "b+")
        val lexer = DefaultTokenizer(listOf(aPlus, bPlus))

        val input = "abaaabbbaaaabbbb"
        val positions = lexer.tokenize(input).toList().map { it.position }

        assertEquals(listOf(0, 1, 2, 5, 8, 12), positions)
    }

    @Test fun rowAndColumn() {
        val aPlus = tokenRegex("aPlus", "a+")
        val bPlus = tokenRegex("bPlus", "b+")
        val br = tokenText("break", "\n")
        val lexer = DefaultTokenizer(listOf(aPlus, bPlus, br))

        val input = """
            aaa
            bbb
            ab
            """.trimIndent()

        val rowCols = lexer.tokenize(input).toList().map { it.row to it.column }

        assertEquals(listOf(1 to 1, 1 to 4,
                            2 to 1, 2 to 4,
                            3 to 1, 3 to 2), rowCols)
    }

    @Test fun mismatchedToken() {
        val a = TokenRegex("a", "a")
        val b = TokenRegex("b", "b")
        val lexer = DefaultTokenizer(listOf(a, b))

        val input = "aabbxxaa"

        val result = lexer.tokenize(input).toList()

        assertEquals(5, result.size)
        assertEquals(noneMatched, result.last().type)
        assertEquals("xxaa", result.last().text)
        assertEquals(4, result.last().position)
    }

    @Test fun priority() {
        val a = tokenRegex("a", "a")
        val aa = tokenText("aa", "aa")

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

    @Test fun tokensWithGroups() {
        val a = tokenRegex("a(b)c")
        val b = tokenRegex("d(e)f")
        val c = tokenRegex("g(h)i")
        val input = "abcdefghi"
        val lex = DefaultTokenizer(listOf(a, b, c))
        val result = lex.tokenize(input)

        assertEquals(listOf(a, b, c), result.toList().map { it.type })
    }
}