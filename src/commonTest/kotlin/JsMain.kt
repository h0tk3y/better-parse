
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.Token

fun main(args: Array<String>) {
    val a = Token("aa", Regex("aa"))
    val tokenizer = DefaultTokenizer(listOf(a))
    println(tokenizer.patterns)
    println(tokenizer.allInOnePattern)
    val match = tokenizer.allInOnePattern.find("aa")
    println(match!!.value)
    val tokens = tokenizer.tokenize("aaaaaa").toList()
    println(tokens)
}