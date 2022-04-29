package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*

/** Tries to parse the sequence with the [parsers] until one succeeds. Returns its [Parsed] result in this case.
 * If none succeeds, returns the [AlternativesFailure] with all the [ErrorResult]s. */
public class OrCombinator<T>(public val parsers: List<Parser<T>>) :
    Parser<T> {

    public override val tokens: List<Token>
        get() = parsers.flatMap { it.tokens }

    override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<T> {
        var failures: ArrayList<ErrorResult>? = null
        for (index in 0 until parsers.size) {
            val result = parsers[index].tryParse(tokens, fromPosition)
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
public infix fun <A> Parser<A>.or(other: Parser<A>): Parser<A> {
    val leftParsers = if (this is OrCombinator) parsers else listOf(this)
    val rightParsers = if (other is OrCombinator) other.parsers else listOf(other)
    return OrCombinator(leftParsers + rightParsers)
}