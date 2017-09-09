package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.Tuple2

/** Parses the sequence with the receiver [Parser] and then with the [other] parser. If both suceed, returns a [Tuple2]
 * with the values from the [Parsed] results. Otherwise, returns the [ErrorResult] of the failed parser. */
infix inline fun <reified A, reified B> Parser<A>.and(other: Parser<B>) =
    AndCombinator(listOf(this, other)) { (a1, a2) -> Tuple2(a1 as A, a2 as B) }

/** The same as `this `[and]` other`*/
operator inline fun <reified A, reified B> Parser<A>.times(other: Parser<B>) = this and other

/** Parses the sequence with the receiver [Parser] and then with the [other] parser. If both suceed, returns a [Tuple2]
 * with the values from the [Parsed] results. Otherwise, returns the [ErrorResult] of the failed parser. */
@JvmName("and0")
infix inline fun <reified A, reified B> AndCombinator<A>.and(other: Parser<B>) =
    AndCombinator(consumers + listOf(other)) { (a1, a2) -> Tuple2(a1 as A, a2 as B) }

/** The same as `this `[and]` other`*/
operator inline fun <reified A, reified B> AndCombinator<A>.times(other: Parser<B>) = this and other

class AndCombinator<out R> internal @PublishedApi constructor(
    val consumers: List<Any>,
    val transform: (List<*>) -> R
) : Parser<R> {

    internal val nonSkippedIndices = consumers.indices.filter { consumers[it] !is SkipParser }

    private fun process(tokens: Sequence<TokenMatch>): Pair<List<ParseResult<*>>, Sequence<TokenMatch>> {
        var lastTokens = tokens
        return consumers.map { consumer ->
            val parser = when (consumer) {
                is Parser<*> -> consumer
                is SkipParser -> consumer.innerParser
                else -> throw IllegalArgumentException()
            }
            val result = parser.tryParse(lastTokens)
            when (result) {
                is ErrorResult -> return@process listOf(result) to lastTokens
                is Parsed<*> -> lastTokens = result.remainder
            }
            result
        } to lastTokens
    }

    override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<R> {
        val (results, remainder) = process(tokens)
        val errorResult = results.singleOrNull() as? ErrorResult
        if (errorResult != null)
            return errorResult

        val values = nonSkippedIndices.map { (results[it] as Parsed).value }

        return Parsed(transform(values), remainder)
    }
}