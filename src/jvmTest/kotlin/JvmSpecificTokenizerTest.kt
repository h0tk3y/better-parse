
import com.github.h0tk3y.betterParse.grammar.token
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.DefaultjvmTokenizer
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class JvmSpecificTokenizerTest {
    @Test
    fun testInputStreamTokenier() {
        val a = token("a")
        val b = token("b")
        val text = "aaabbabbba"
        val defaultTokenizer = DefaultTokenizer(listOf(a, b))
        val jvmTokenizer = DefaultjvmTokenizer(listOf(a, b))

        val expected = defaultTokenizer.tokenize(text).toList()
        val actualWithInputStream = jvmTokenizer.tokenize(text.byteInputStream()).toList()
        val actualWithReadable = jvmTokenizer.tokenize(text.byteInputStream().bufferedReader()).toList()
        val actualWithScanner = jvmTokenizer.tokenize(Scanner(text)).toList()

        assertEquals(expected, actualWithInputStream)
        assertEquals(expected, actualWithReadable)
        assertEquals(expected, actualWithScanner)
    }
}