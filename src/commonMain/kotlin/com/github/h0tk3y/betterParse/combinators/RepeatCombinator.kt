package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*

/** Tries to parse the sequence with the [parser], as many times as it succeeds, but no more than [atMost].
 * If the parser succeeded less than [atLeast] times, returns its [ErrorResult], otherwise returns the list of [Parsed]
 * results from the parser invocations.*/
public class RepeatCombinator<T> internal constructor(
    public val parser: Parser<T>,
    public val atLeast: Int = 0,
    public val atMost: Int = -1
) : Parser<List<T>>, TokenProvider by parser {

    init {
        require(atLeast >= 0) { "atLeast = $atLeast, expected non-negative" }
        require(atMost == -1 || atMost >= atLeast) { "atMost = $atMost is invalid, should be greater or equal than atLeast = $atLeast" }
    }

    override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<List<T>> {
        val resultsList = arrayListOf<T>()
        var nextPosition = fromPosition
        while (atMost == -1 || resultsList.size < atMost) {
            val result = parser.tryParse(tokens, nextPosition)
            when (result) {
                is ErrorResult -> {
                    return if (resultsList.size >= atLeast)
                        ParsedValue(resultsList, nextPosition)
                    else
                        result
                }
                is Parsed -> {
                    resultsList.add(result.value)
                    nextPosition = result.nextPosition
                }
            }
        }
        return ParsedValue(resultsList, nextPosition)
    }
}

public fun <T> zeroOrMore(parser: Parser<T>): Parser<List<T>> = RepeatCombinator(parser)

public fun <T> oneOrMore(parser: Parser<T>): Parser<List<T>> = RepeatCombinator(parser, atLeast = 1)

public infix fun <T> Int.times(parser: Parser<T>): Parser<List<T>> =
    RepeatCombinator(parser, atLeast = this, atMost = this)

public infix fun <T> IntRange.times(parser: Parser<T>): Parser<List<T>> =
    RepeatCombinator(parser, atLeast = first, atMost = last)

public infix fun <T> Int.timesOrMore(parser: Parser<T>): Parser<List<T>> = RepeatCombinator(parser, atLeast = this)