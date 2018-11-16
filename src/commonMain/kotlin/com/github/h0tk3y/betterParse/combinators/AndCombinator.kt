package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.Tuple2
import kotlin.jvm.JvmName

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

class AndCombinator<out R> @PublishedApi internal constructor(
    val consumers: List<Any>,
    val transform: (List<Any?>) -> R
) : Parser<R> {

    internal val nonSkippedIndices = consumers.indices.filter { consumers[it] !is SkipParser }

    override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<R> {
        var remainder = tokens

        val results = arrayListOf<Any>()
        loop@for (index in 0 until consumers.size) {
            val parser = when (val consumer = consumers[index]) {
                is Parser<*> -> consumer
                is SkipParser -> consumer.innerParser
                else -> throw IllegalArgumentException()
            }
            val result = parser.tryParse(remainder)
            when (result) {
                is ErrorResult -> {
                    results.clear()
                    results.add(result)
                    break@loop
                }
                is Parsed<*> -> {
                    results.add(result)
                    remainder = result.remainder
                }
            }
        }

        val errorResult = results.singleOrNull() as? ErrorResult
        if (errorResult != null)
            return errorResult

        val values = nonSkippedIndices.map { (results[it] as Parsed<*>).value }

        return Parsed(transform(values), remainder)
    }
}