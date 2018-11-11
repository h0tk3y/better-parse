package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.*

/** Tries to parse the sequence with the [parsers] until one succeeds. Returns its [Parsed] result in this case.
 * If none succeeds, returns the [AlternativesFailure] with all the [ErrorResult]s. */
class OrCombinator<T>(val parsers: List<Parser<T>>) :
    Parser<T> {
    override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<T> {
        var failures: ArrayList<ErrorResult>? = null
        for (parser in parsers) {
            val result = parser.tryParse(tokens)
            when (result) {
                is Parsed -> return result
                is ErrorResult -> {
                    if (failures == null)
                        failures = ArrayList()
                    failures.add(result)
                }
            }
        }
        return AlternativesFailure(failures.orEmpty())
    }
}

/** Parse the sequence with either the receiver [Parser] or the [other] parser. See [OrCombinator] */
infix fun <A> Parser<A>.or(other: Parser<A>): Parser<A> {
    val leftParsers = if (this is OrCombinator) parsers else listOf(this)
    val rightParsers = if (other is OrCombinator) other.parsers else listOf(other)
    return OrCombinator(leftParsers + rightParsers)
}