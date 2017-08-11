package com.github.h0tk3y.betterParse.grammar

import com.github.h0tk3y.betterParse.lexer.Lexer
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import com.github.h0tk3y.betterParse.parser.tryParseToEnd
import org.intellij.lang.annotations.RegExp
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern
import kotlin.reflect.KProperty

/**
 * A language grammar represented by a list of [Token]s and one or more [Parser]s, with one
 * specific [rootParser] that accepts the words of this [Grammar].
 */
abstract class Grammar<out T> : Parser<T> {

    private val _tokens = arrayListOf<Token>()

    private val _parsers = hashSetOf<Parser<*>>()

    /** List of tokens that is by default used for tokenizing a sequence before parsing this language. The tokens are
     * added to this list during an instance construction. */
    open val tokens get(): List<Token> = _tokens

    open val declaredParsers get() = _parsers + _tokens + rootParser

    /** Creates a [TokenDelegate] for simple [Token] definition within an implementation of this [Grammar]. */
    protected fun token(@RegExp pattern: String, ignore: Boolean = false) = TokenDelegate(pattern.toPattern(), ignore, _tokens)

    /** Creates a [TokenDelegate] for simple [Token] definition within an implementation of this [Grammar]. */
    protected fun token(pattern: Pattern, ignore: Boolean = false) = TokenDelegate(pattern, ignore, _tokens)

    /** Creates a [TokenDelegate] for simple [Token] definition within an implementation of this [Grammar]. */
    protected fun token(pattern: Regex, ignore: Boolean = false) = TokenDelegate(pattern.toPattern(), ignore, _tokens)


    /** A [Lexer] that is built with the [Token]s defined within this [Grammar], in their order of declaration */
    val lexer by lazy { Lexer(tokens) }

    /** A [Parser] that represents the root rule of this [Grammar] and is used by default for parsing. */
    abstract val rootParser: Parser<T>

    final override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<T> = rootParser.tryParse(tokens)

    protected operator fun <T> Parser<T>.provideDelegate(thisRef: Grammar<*>, property: KProperty<*>): Parser<T> {
        _parsers.add(this)
        return this
    }

    protected operator fun <T> Parser<T>.getValue(thisRef: Grammar<*>, property: KProperty<*>): Parser<T> {
        return this
    }
}

/** Constructs a [Token] and, if provided, adds it to the [addTo] collection upon the token construction. */
class TokenDelegate (
    val pattern: Pattern,
    val canBeIgnored: Boolean = false,
    val addTo: MutableCollection<in Token>? = null
) {
    lateinit private var token: Token

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): TokenDelegate {
        token = Token(property.name, pattern, canBeIgnored).apply { addTo?.add(this) }
        return this
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Token = token
}

fun token(pattern: String, addTo: MutableCollection<in Token>? = null, ignore: Boolean = false) = TokenDelegate(pattern.toPattern(), ignore, addTo)
fun token(pattern: Pattern, addTo: MutableCollection<in Token>? = null, ignore: Boolean = false) = TokenDelegate(pattern, ignore, addTo)
fun token(pattern: Regex, addTo: MutableCollection<in Token>? = null, ignore: Boolean = false) = TokenDelegate(pattern.toPattern(), ignore, addTo)

/** A convenience function to use for referencing a parser that is not initialized up to this moment. */
fun <T> parser(block: () -> Parser<T>): Parser<T> = ParserReference(block)

class ParserReference<out T> internal constructor(parserProvider: () -> Parser<T>) : Parser<T> {
    val parser by lazy(parserProvider)
    override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<T> = parser.tryParse(tokens)
}

fun <T> Grammar<T>.tryParseToEnd(input: String) = rootParser.tryParseToEnd(lexer.tokenize(input))
fun <T> Grammar<T>.tryParseToEnd(input: InputStream) = rootParser.tryParseToEnd(lexer.tokenize(input))
fun <T> Grammar<T>.tryParseToEnd(input: Readable) = rootParser.tryParseToEnd(lexer.tokenize(input))
fun <T> Grammar<T>.tryParseToEnd(input: Scanner) = rootParser.tryParseToEnd(lexer.tokenize(input))

fun <T> Grammar<T>.parseToEnd(input: String): T = rootParser.parseToEnd(lexer.tokenize(input))
fun <T> Grammar<T>.parseToEnd(input: InputStream): T = rootParser.parseToEnd(lexer.tokenize(input))
fun <T> Grammar<T>.parseToEnd(input: Readable): T = rootParser.parseToEnd(lexer.tokenize(input))
fun <T> Grammar<T>.parseToEnd(input: Scanner): T = rootParser.parseToEnd(lexer.tokenize(input))