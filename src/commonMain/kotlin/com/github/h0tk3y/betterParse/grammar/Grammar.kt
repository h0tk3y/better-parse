package com.github.h0tk3y.betterParse.grammar

import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import com.github.h0tk3y.betterParse.parser.tryParseToEnd
import kotlin.reflect.KProperty

/**
 * A language grammar represented by a list of [Token]s and one or more [Parser]s, with one
 * specific [rootParser] that accepts the words of this [Grammar].
 */
abstract class Grammar<out T> : Parser<T> {

    private val _tokens = arrayListOf<Token>()

    private val _parsers = linkedSetOf<Parser<*>>()

    /** List of tokens that is by default used for tokenizing a sequence before parsing this language. The tokens are
     * added to this list during an instance construction. */
    open val tokens get(): List<Token> = _tokens

    /** Set of the tokens and parsers that were declared by delegation to the parser instances (`val p by someParser`), and [rootParser] */
    open val declaredParsers get() = (_parsers + _tokens + rootParser).toSet()
    
    /** A [Tokenizer] that is built with the [Token]s defined within this [Grammar], in their order of declaration */
    open val tokenizer: Tokenizer by lazy { DefaultTokenizer(tokens) }

    /** A [Parser] that represents the root rule of this [Grammar] and is used by default for parsing. */
    abstract val rootParser: Parser<T>

    final override fun tryParse(tokens: TokenMatchesSequence): ParseResult<T> = rootParser.tryParse(tokens)

    protected operator fun <T> Parser<T>.provideDelegate(thisRef: Grammar<*>, property: KProperty<*>): Parser<T> =
        also { _parsers.add(it) }

    protected operator fun <T> Parser<T>.getValue(thisRef: Grammar<*>, property: KProperty<*>): Parser<T> = this

    protected operator fun Token.provideDelegate(thisRef: Grammar<*>, property: KProperty<*>) : Token =
        also {
            if (it.name == null) {
                it.name = property.name
            }
            _tokens.add(it)
        }

    protected operator fun Token.getValue(thisRef: Grammar<*>, property: KProperty<*>) : Token = this
}

/** A convenience function to use for referencing a parser that is not initialized up to this moment. */
fun <T> parser(block: () -> Parser<T>): Parser<T> = ParserReference(block)

expect class ParserReference<out T> internal constructor(parserProvider: () -> Parser<T>) : Parser<T> {
    val parser: Parser<T>
}

fun <T> Grammar<T>.tryParseToEnd(input: String) = rootParser.tryParseToEnd(tokenizer.tokenize(input))

fun <T> Grammar<T>.parseToEnd(input: String): T = rootParser.parseToEnd(tokenizer.tokenize(input))