package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser

/** Parses the sequence with [innerParser], and if that succeeds, maps its [Parsed] result with [transform].
 * Then run this mapped result on any remaining input.
 * Returns the [ErrorResult] of the `innerParser` otherwise.
 * @sample BindTest*/
class BindCombinator<T, R>(
    val innerParser: Parser<T>,
    val transform: (T) -> Parser<R>
) : Parser<R> {
    override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<R> {
        val innerResult = innerParser.tryParse(tokens)
        return when (innerResult) {
            is ErrorResult -> innerResult
            is Parsed -> { transform(innerResult.value).tryParse(innerResult.remainder) }
        }
    }
}

/** Applies the [transform] function to the successful results of the receiver parser and then runs
 *  the new parser on any remaining input. See [BindCombinator]. */
infix fun <A, T> Parser<A>.bind(transform: (A) -> Parser<T>): Parser<T> = BindCombinator(this, transform)

/** Applies the [transform] receiver to the successful results of the receiver parser and then runs
 *  the new parser on any remaining input. See [BindCombinator]. */
infix fun <A, T> Parser<A>.useBind(transform: A.() -> Parser<T>): Parser<T> = BindCombinator(this, transform)