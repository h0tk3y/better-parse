package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.parser.SuccessResult
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.Tuple
import com.github.h0tk3y.betterParse.utils.Tuple1

/** Wraps a [Parser] to distinguish it from other parsers when it is used in [and] functions. */
class SkipParser(val innerParser: Parser<*>)

/** Wraps a [Parser] to distinguish it from other parsers when it is used in [and] functions. */
fun <T> skip(parser: Parser<T>) = SkipParser(parser)

/** The same as `[skip] of this parser. ` */
operator fun Parser<*>.unaryMinus() = skip(this)

/** Parses the sequence with the receiver [Parser] and the wrapped [other] parser, but returns the [SuccessResult] result
 * from the receiver parser. */
infix fun <T : Tuple> AndCombinator<T>.and(other: SkipParser) =
    AndCombinator(consumers + other, transform)

operator fun <T : Tuple> AndCombinator<T>.times(other: SkipParser) = this and other

/** Parses the sequence with the receiver [Parser] and the wrapped [other] parser, but returns the [SuccessResult] result
 * with a value from the receiver parser in a [Tuple1]. */
infix inline fun <reified T> Parser<T>.and(other: SkipParser) =
    AndCombinator(listOf(this, other), { (a) -> a as T } )

/** The same as `this `[and]` other`*/
operator inline fun <reified T> Parser<T>.times(other: SkipParser) = this and other

/** Parses the wrapped receiver [Parser] and the [other] parser and returns the [SuccessResult] result
 * with a value from the [other] parser in a [Tuple1]. */
infix inline fun <reified T> SkipParser.and(other: Parser<T>) =
    AndCombinator(listOf(this, other)) { (b) -> b as T }

/** The same as `this `[and]` other`*/
operator inline fun <reified T> SkipParser.times(other: Parser<T>) = this and other

infix fun SkipParser.and(other: SkipParser): SkipParser {
    val parsersLeft = if (innerParser is AndCombinator) innerParser.consumers else listOf(innerParser)
    return SkipParser(AndCombinator(parsersLeft + other.innerParser, { Unit }))
}

/** The same as `this `[and]` other`*/
operator fun SkipParser.times(other: SkipParser) = this and other