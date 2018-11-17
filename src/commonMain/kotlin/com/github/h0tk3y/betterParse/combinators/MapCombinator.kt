package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*

/** Parses the sequence with [innerParser], and if that succeeds, maps its [SuccessResult] result with [transform].
 * Returns the [ErrorResult] of the `innerParser` otherwise.
 * @sample MapTest*/
class MapCombinator<T, R>(
    val innerParser: Parser<T>,
    val transform: (T) -> R
) : Parser<R> {
    override fun tryParse(tokens: TokenMatchesSequence, position: Int): ParseResult<R> {
        val innerResult = innerParser.tryParse(tokens, position)
        return when (innerResult) {
            is ErrorResult -> innerResult
            is SuccessResult -> Parsed(transform(innerResult.value), innerResult.nextTokenIndex)
        }
    }
}

/** Applies the [transform] function to the successful results of the receiver parser. See [MapCombinator]. */
infix fun <A, T> Parser<A>.map(transform: (A) -> T): Parser<T> = MapCombinator(this, transform)

/** Applies the [transform] extension to the successful results of the receiver parser. See [MapCombinator]. */
infix fun <A, T> Parser<A>.use(transform: A.() -> T): Parser<T> = MapCombinator(this, transform)

/** Replaces the [SuccessResult] result of the receiver parser with the provided [value]. */
infix fun <A, T> Parser<A>.asJust(value: T) = MapCombinator(this) { value }