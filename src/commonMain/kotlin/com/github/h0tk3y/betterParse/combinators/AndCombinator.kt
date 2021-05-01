package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence
import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.utils.Tuple2
import kotlin.jvm.JvmName

/** Parses the sequence with the receiver [Parser] and then with the [other] parser. If both succeed, returns a [Tuple2]
 * with the values from the [Parsed] results. Otherwise, returns the [ErrorResult] of the failed parser. */
public inline infix fun <reified A, reified B> Parser<A>.and(other: Parser<B>): AndCombinator<Tuple2<A, B>> =
    AndCombinator(listOf(this, other)) { (a1, a2) -> Tuple2(a1 as A, a2 as B) }

/** The same as `this `[and]` other`*/
public inline operator fun <reified A, reified B> Parser<A>.times(other: Parser<B>): AndCombinator<Tuple2<A, B>> =
    this and other

/** Parses the sequence with the receiver [Parser] and then with the [other] parser. If both succeed, returns a [Tuple2]
 * with the values from the [Parsed] results. Otherwise, returns the [ErrorResult] of the failed parser. */
@JvmName("and0")
public inline infix fun <reified A, reified B> AndCombinator<A>.and(other: Parser<B>): AndCombinator<Tuple2<A, B>> =
    AndCombinator(consumersImpl + listOf(other)) { (a1, a2) -> Tuple2(a1 as A, a2 as B) }

/** The same as `this `[and]` other`*/
public inline operator fun <reified A, reified B> AndCombinator<A>.times(other: Parser<B>): AndCombinator<Tuple2<A, B>> =
    this and other

public class AndCombinator<out R> @PublishedApi internal constructor(
    @PublishedApi internal val consumersImpl: List<Any>,
    internal val transform: (List<Any?>) -> R
) : MemoizedParser<R>() {

    @Deprecated(
        "Use parsers or skipParsers instead to get the type-safe results.", replaceWith = ReplaceWith("parsers")
    )
    public val consumers: List<Any>
        get() = consumersImpl

    public val parsers: List<Parser<*>?>
        get() = consumersImpl.map { it as? Parser<*> }

    public val skipParsers: List<SkipParser?>
        get() = consumersImpl.map { it as? SkipParser }

    internal val nonSkippedIndices = consumersImpl.indices.filter { consumersImpl[it] !is SkipParser }

    override fun tryParseImpl(
        tokens: TokenMatchesSequence,
        fromPosition: Int,
        context: ParsingContext
    ): ParseResult<R> {
        var nextPosition = fromPosition

        var results: ArrayList<Any?>? = null
        loop@ for (index in 0 until consumersImpl.size) {
            val consumer = consumersImpl[index]
            when (consumer) {
                is Parser<*> -> {
                    val result = consumer.tryParseWithContextIfSupported(tokens, nextPosition, context)
                    when (result) {
                        is ErrorResult -> return result
                        is Parsed<*> -> {
                            (results ?: ArrayList<Any?>().also { results = it }).add(result.value)
                            nextPosition = result.nextPosition
                        }
                    }
                }
                is SkipParser -> {
                    val result = consumer.innerParser.tryParseWithContextIfSupported(tokens, nextPosition, context)
                    when (result) {
                        is ErrorResult -> return result
                        is Parsed<*> -> nextPosition = result.nextPosition
                    }
                }
                else -> throw IllegalArgumentException()
            }
        }

        return ParsedValue(transform(results ?: emptyList()), nextPosition)
    }
}