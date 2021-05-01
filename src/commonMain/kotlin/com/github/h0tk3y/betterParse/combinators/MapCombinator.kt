package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*

/** Parses the sequence with [innerParser], and if that succeeds, maps its [Parsed] result with [transform].
 * Returns the [ErrorResult] of the `innerParser` otherwise.
 * @sample MapTest*/
public class MapCombinator<T, R>(
    public val innerParser: Parser<T>,
    public val transform: (T) -> R
) : MemoizedParser<R>() {
    override fun tryParseImpl(
        tokens: TokenMatchesSequence,
        fromPosition: Int,
        context: ParsingContext
    ): ParseResult<R> {
        val innerResult = innerParser.tryParseWithContextIfSupported(tokens, fromPosition, context)
        return when (innerResult) {
            is ErrorResult -> innerResult
            is Parsed -> ParsedValue(transform(innerResult.value), innerResult.nextPosition)
        }
    }
}

/** Applies the [transform] function to the successful results of the receiver parser. See [MapCombinator]. */
public infix fun <A, T> Parser<A>.map(transform: (A) -> T): Parser<T> = MapCombinator(this, transform)

/** Applies the [transform] extension to the successful results of the receiver parser. See [MapCombinator]. */
public infix fun <A, T> Parser<A>.use(transform: A.() -> T): Parser<T> = MapCombinator(this, transform)

/** Replaces the [Parsed] result of the receiver parser with the provided [value]. */
public infix fun <A, T> Parser<A>.asJust(value: T): MapCombinator<A, T> = MapCombinator(this) { value }