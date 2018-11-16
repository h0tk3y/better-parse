
import com.github.h0tk3y.betterParse.lexer.*
import java.util.*
import kotlin.test.*

class JvmSpecificTokenizerTest {
    @Test
    fun testInputStreamTokenier() {
        val a = tokenRegex("a")
        val b = tokenRegex("b")
        val text = "aaabbabbba"
        val defaultTokenizer = DefaultTokenizer(listOf(a, b))
        val jvmTokenizer = DefaultJvmTokenizer(listOf(a, b))

        val expected = defaultTokenizer.tokenize(text).toList()
        val actualWithInputStream = jvmTokenizer.tokenize(text.byteInputStream()).toList()
        val actualWithReadable = jvmTokenizer.tokenize(text.byteInputStream().bufferedReader()).toList()
        val actualWithScanner = jvmTokenizer.tokenize(Scanner(text)).toList()

        assertEquals(expected, actualWithInputStream)
        assertEquals(expected, actualWithReadable)
        assertEquals(expected, actualWithScanner)
    }
}