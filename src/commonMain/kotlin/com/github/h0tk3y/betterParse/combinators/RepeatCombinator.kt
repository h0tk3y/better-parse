package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*

/** Tries to parse the sequence with the [parser], as many times as it succeeds, but no more than [atMost].
 * If the parser succeeded less than [atLeast] times, returns its [ErrorResult], otherwise returns the list of [Parsed]
 * results from the parser invocations.*/
class RepeatCombinator<T> internal constructor(
    val parser: Parser<T>,
    val atLeast: Int = 0,
    val atMost: Int = -1
) : Parser<List<T>> {

    init {
        require(atLeast >= 0) { "atLeast = $atLeast, expected non-negative" }
        require(atMost == -1 || atMost >= atLeast) { "atMost = $atMost is invalid, should be greater or equal than atLeast = $atLeast" }
    }

    override fun tryParse(tokens: TokenMatchesSequence): ParseResult<List<T>> {
        val resultsList = arrayListOf<T>()
        var lastTokens = tokens
        while (atMost == -1 || resultsList.size < atMost) {
            val item = parser.tryParse(lastTokens)
            when (item) {
                is ErrorResult -> {
                    return if (resultsList.size >= atLeast)
                        Parsed(resultsList, lastTokens)
                    else
                        item
                }
                is Parsed -> {
                    resultsList.add(item.value)
                    lastTokens = item.remainder
                }
            }
        }
        return Parsed(resultsList, lastTokens)
    }
}

fun <T> zeroOrMore(parser: Parser<T>): Parser<List<T>> = RepeatCombinator(parser)

fun <T> oneOrMore(parser: Parser<T>): Parser<List<T>> = RepeatCombinator(parser, atLeast = 1)

infix fun <T> Int.times(parser: Parser<T>): Parser<List<T>> = RepeatCombinator(parser, atLeast = this, atMost = this)

infix fun <T> IntRange.times(parser: Parser<T>): Parser<List<T>> = RepeatCombinator(parser, atLeast = first, atMost = last)

infix fun <T> Int.timesOrMore(parser: Parser<T>): Parser<List<T>> = RepeatCombinator(parser, atLeast = this)