
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.Token

fun main(args: Array<String>) {
    val a = Token("aa", Regex("aa"))
    val tokenizer = DefaultTokenizer(listOf(a))
    val tokens = tokenizer.tokenize("aaaaaa").toList()
    println(tokens)
}