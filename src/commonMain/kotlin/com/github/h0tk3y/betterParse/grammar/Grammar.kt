package com.github.h0tk3y.betterParse.grammar

import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence
import com.github.h0tk3y.betterParse.lexer.Tokenizer
import com.github.h0tk3y.betterParse.parser.*
import kotlin.reflect.KProperty

/**
 * A language grammar represented by a list of [Token]s and one or more [Parser]s, with one
 * specific [rootParser] that accepts the words of this [Grammar].
 */
public abstract class Grammar<out T> : Parser<T> {

    /** Tokens declared in concrete object are added here during instance construction. */
    private val _tokens = arrayListOf<Token>()

    private val _parsers = linkedSetOf<Parser<*>>()

    /**
     * List of tokens that is by default used for tokenizing a sequence before parsing.
     */
    public override val tokens: List<Token> by lazy { (_tokens + rootParser.tokens).distinctBy { it.name ?: it } }

    /** Set of the tokens and parsers that were declared by delegation to the parser instances (`val p by someParser`), and [rootParser] */
    public open val declaredParsers: Set<Parser<*>> by lazy { (_parsers + tokens + rootParser).toSet() }

    /** A [Tokenizer] that is built with the [Token]s defined within this [Grammar], in their order of declaration */
    public open val tokenizer: Tokenizer by lazy { DefaultTokenizer(tokens) }

    /** A [Parser] that represents the root rule of this [Grammar] and is used by default for parsing. */
    public abstract val rootParser: Parser<T>

    final override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<T> = rootParser.tryParse(
        tokens, fromPosition
    )

    protected operator fun <T> Parser<T>.provideDelegate(thisRef: Grammar<*>, property: KProperty<*>): Parser<T> =
        also { _parsers.add(it) }

    protected operator fun <T> Parser<T>.getValue(thisRef: Grammar<*>, property: KProperty<*>): Parser<T> = this

    protected operator fun Token.provideDelegate(thisRef: Grammar<*>, property: KProperty<*>): Token =
        also {
            if (it.name == null) {
                it.name = property.name
            }
            _tokens.add(it)
        }
}

/** A convenience function to use for referencing a parser that is not initialized up to this moment. */
public fun <T> parser(block: () -> Parser<T>): Parser<T> = ParserReference(block)

public class ParserReference<out T> internal constructor(parserProvider: () -> Parser<T>) : Parser<T> {
    public val parser: Parser<T> by lazy(parserProvider)

    public override val tokens: List<Token> = listOf() // a parser reference defines no tokens itself

    override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<T> =
        parser.tryParse(tokens, fromPosition)
}

public fun <T> Grammar<T>.tryParseToEnd(input: String): ParseResult<T> =
    rootParser.tryParseToEnd(tokenizer.tokenize(input), 0)

public fun <T> Grammar<T>.parseToEnd(input: String): T =
    rootParser.parseToEnd(tokenizer.tokenize(input))

public fun <T> grammar(rootParser: Parser<T>): Grammar<T> {
    return object : Grammar<T>() {
        override val rootParser: Parser<T> = rootParser
    }
}