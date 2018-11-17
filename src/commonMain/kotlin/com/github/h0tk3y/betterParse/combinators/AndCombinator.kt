package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.utils.*
import kotlin.jvm.*

/** Parses the sequence with the receiver [Parser] and then with the [other] parser. If both suceed, returns a [Tuple2]
 * with the values from the [SuccessResult] results. Otherwise, returns the [ErrorResult] of the failed parser. */
infix inline fun <reified A, reified B> Parser<A>.and(other: Parser<B>) =
    AndCombinator(listOf(this, other)) { (a1, a2) -> Tuple2(a1 as A, a2 as B) }

/** The same as `this `[and]` other`*/
operator inline fun <reified A, reified B> Parser<A>.times(other: Parser<B>) = this and other

/** Parses the sequence with the receiver [Parser] and then with the [other] parser. If both suceed, returns a [Tuple2]
 * with the values from the [SuccessResult] results. Otherwise, returns the [ErrorResult] of the failed parser. */
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

    override fun tryParse(tokens: TokenMatchesSequence, position: Int): ParseResult<R> {
        var nextPosition = position

        var results: ArrayList<Any?>? = null
        loop@ for (index in 0 until consumers.size) {
            val consumer = consumers[index]
            when (consumer) {
                is Parser<*> -> {
                    val result = consumer.tryParse(tokens, nextPosition)
                    when (result) {
                        is ErrorResult -> return result
                        is SuccessResult<*> -> {
                            (results ?: ArrayList<Any?>().also { results = it }).add(result.value)
                            nextPosition = result.nextTokenIndex
                        }
                    }
                }
                is SkipParser -> {
                    val result = consumer.innerParser.tryParse(tokens, nextPosition)
                    when (result) {
                        is ErrorResult -> return result
                        is SuccessResult<*> -> nextPosition = result.nextTokenIndex
                    }
                }
                else -> throw IllegalArgumentException()
            }
        }

        return Parsed(transform(results ?: emptyList()), nextPosition)
    }
}