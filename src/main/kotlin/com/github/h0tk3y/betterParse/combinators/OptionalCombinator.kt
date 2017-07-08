package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser

/** Tries to parse the sequence with [parser], and if that fails, returns [Parsed] of null instead. */
class OptionalCombinator<T>(val parser: Parser<T>) : Parser<T?> {
    override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<T?> {
        val result = parser.tryParse(tokens)
        return when (result) {
            is ErrorResult -> Parsed(null, tokens)
            is Parsed -> result
        }
    }
}

/** Uses [parser] and if that failes returns [Parsed] of null. */
fun <T> optional(parser: Parser<T>) : Parser<T?> = OptionalCombinator(parser)