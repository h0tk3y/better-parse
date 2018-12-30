
import com.github.h0tk3y.betterParse.lexer.*

fun main(args: Array<String>) {
    val a = RegexToken("aa", Regex("aa"))
    val tokenizer = DefaultTokenizer(listOf(a))
    val tokens = tokenizer.tokenize("aaaaaa").toList()
    println(tokens)
}
