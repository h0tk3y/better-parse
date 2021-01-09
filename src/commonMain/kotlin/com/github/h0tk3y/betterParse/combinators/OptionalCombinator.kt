package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*

/** Tries to parse the sequence with [parser], and if that fails, returns [Parsed] of null instead. */
public class OptionalCombinator<T>(public val parser: Parser<T>) :
    Parser<T?> {
    override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<T?> {
        val result = parser.tryParse(tokens, fromPosition)
        return when (result) {
            is ErrorResult -> ParsedValue(null, fromPosition)
            is Parsed -> result
        }
    }
}

/** Uses [parser] and if that fails returns [Parsed] of null. */
public fun <T> optional(parser: Parser<T>): Parser<T?> = OptionalCombinator(parser)