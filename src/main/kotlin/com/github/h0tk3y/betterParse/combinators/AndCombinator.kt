package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.Tuple
import com.github.h0tk3y.betterParse.utils.Tuple1
import com.github.h0tk3y.betterParse.utils.Tuple2

/** Parses the sequence with the receiver [Parser] and then with the [other] parser. If both suceed, returns a [Tuple2]
 * with the values from the [Parsed] results. Otherwise, returns the [ErrorResult] of the failed parser. */
infix inline fun <reified A, reified B> Parser<A>.and(other: Parser<B>) =
    AndCombinator(listOf(this, other)) { (a1, a2) -> Tuple2(a1 as A, a2 as B) }

/** Parses the sequence with the receiver [Parser] and then with the [other] parser. If both suceed, returns a [Tuple2]
 * with the values from the [Parsed] results. Otherwise, returns the [ErrorResult] of the failed parser. */
infix inline fun <reified A, reified B> AndCombinator<Tuple1<A>>.and(other: Parser<B>) =
    AndCombinator(consumers + other) { (a1, a2) -> Tuple2(a1 as A, a2 as B) }

class AndCombinator<out R : Tuple> internal @PublishedApi constructor(
    val consumers: List<Any>,
    val transform: (List<*>) -> R
) : Parser<R> {

    fun process(tokens: Sequence<TokenMatch>): Pair<List<ParseResult<*>>, Sequence<TokenMatch>> {
        3.1 in 1..3

        var lastTokens = tokens
        var errorResult: ErrorResult? = null
        return consumers.map { consumer ->
            val parser = when (consumer) {
                is Parser<*> -> consumer
                is SkipParser<*> -> consumer.innerParser
                else -> throw IllegalArgumentException()
            }
            val result = errorResult ?: parser.tryParse(lastTokens)
            when (result) {
                is ErrorResult -> errorResult = result
                is Parsed<*> -> lastTokens = result.remainder
            }
            if (consumer is SkipParser<*>) null else result
        }.filterNotNull() to lastTokens
    }

    override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<R> {
        val (results, remainder) = process(tokens)
        return results.firstOrNull { it is ErrorResult } as? ErrorResult
               ?: results.filterIsInstance<Parsed<*>>().let { Parsed(transform(it.map { it.value }), remainder) }
    }
}

/** Wraps a [Parser] to distinguish it from other parsers when it is used in [and] functions. */
class SkipParser<T>(val innerParser: Parser<T>)

/** Wraps a [Parser] to distinguish it from other parsers when it is used in [and] functions. */
fun <T> skip(parser: Parser<T>) = SkipParser(parser)

/** Parses the sequence with the receiver [Parser] and the wrapped [other] parser, but returns the [Parsed] result
 * from the receiver parser. */
infix fun <T : Tuple, B> AndCombinator<T>.and(other: SkipParser<B>) =
    AndCombinator(consumers + other, transform)

/** Parses the sequence with the receiver [Parser] and the wrapped [other] parser, but returns the [Parsed] result
 * with a value from the receiver parser in a [Tuple1]. */
infix inline fun <reified T> Parser<T>.and(other: SkipParser<*>) =
    AndCombinator(listOf(this, other), { (a) -> Tuple1(a as T) })

/** Parses the wrapped receiver [Parser] and the [other] parser and returns the [Parsed] result
 * with a value from the [other] parser in a [Tuple1]. */
infix inline fun <reified T> SkipParser<*>.and(other: Parser<T>) =
    AndCombinator(listOf(this, other)) { (b) -> Tuple1(b as T) }