package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*

/** Tries to parse the sequence with [parser], and if that fails, returns [Parsed] of null instead. */
class OptionalCombinator<T>(val parser: Parser<T>) :
    Parser<T?> {
    override fun tryParse(tokens: TokenMatchesSequence, position: Int): ParseResult<T?> {
        val result = parser.tryParse(tokens, position)
        return when (result) {
            is ErrorResult -> Parsed(null, position)
            is Parsed -> result
        }
    }
}

/** Uses [parser] and if that failes returns [Parsed] of null. */
fun <T> optional(parser: Parser<T>): Parser<T?> = OptionalCombinator(parser)