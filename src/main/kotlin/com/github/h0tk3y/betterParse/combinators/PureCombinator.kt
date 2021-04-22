package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser

/** Returns [Parsed] of [value] without consuming any input */
class PureCombinator<T>(val value: T) : Parser<T> {
    override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<T> = Parsed(value, tokens)
}

/** Returns [Parsed] of [value] without consuming any input */
fun <T> pure(value: T) : Parser<T> = PureCombinator(value)