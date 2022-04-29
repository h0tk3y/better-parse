package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.TokenProvider
import com.github.h0tk3y.betterParse.utils.Tuple
import com.github.h0tk3y.betterParse.utils.Tuple1

/** Wraps a [Parser] to distinguish it from other parsers when it is used in [and] functions. */
public class SkipParser(public val innerParser: Parser<*>): TokenProvider by innerParser

/** Wraps a [Parser] to distinguish it from other parsers when it is used in [and] functions. */
public fun <T> skip(parser: Parser<T>): SkipParser = SkipParser(parser)

/** The same as `[skip] of this parser. ` */
public operator fun Parser<*>.unaryMinus(): SkipParser = skip(this)

/** Parses the sequence with the receiver [Parser] and the wrapped [other] parser, but returns the [Parsed] result
 * from the receiver parser. */
public infix fun <T : Tuple> AndCombinator<T>.and(other: SkipParser): AndCombinator<T> =
    AndCombinator(consumersImpl + other, transform)

public operator fun <T : Tuple> AndCombinator<T>.times(other: SkipParser): AndCombinator<T> = this and other

/** Parses the sequence with the receiver [Parser] and the wrapped [other] parser, but returns the [Parsed] result
 * with a value from the receiver parser in a [Tuple1]. */
public inline infix fun <reified T> Parser<T>.and(other: SkipParser): AndCombinator<T> =
    AndCombinator(listOf(this, other), { (a) -> a as T } )

/** The same as `this `[and]` other`*/
public inline operator fun <reified T> Parser<T>.times(other: SkipParser): AndCombinator<T> = this and other

/** Parses the wrapped receiver [Parser] and the [other] parser and returns the [Parsed] result
 * with a value from the [other] parser in a [Tuple1]. */
public inline infix fun <reified T> SkipParser.and(other: Parser<T>): AndCombinator<T> =
    AndCombinator(listOf(this, other)) { (b) -> b as T }

/** The same as `this `[and]` other`*/
public inline operator fun <reified T> SkipParser.times(other: Parser<T>): AndCombinator<T> = this and other

public infix fun SkipParser.and(other: SkipParser): SkipParser {
    val parsersLeft = if (innerParser is AndCombinator) innerParser.consumersImpl else listOf(innerParser)
    return SkipParser(AndCombinator(parsersLeft + other.innerParser, { }))
}

/** The same as `this `[and]` other`*/
public operator fun SkipParser.times(other: SkipParser): SkipParser = this and other