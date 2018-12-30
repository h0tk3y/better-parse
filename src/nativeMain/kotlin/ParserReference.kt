package com.github.h0tk3y.betterParse.grammar

import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parser

actual class ParserReference<out T> internal actual constructor(
    private val parserProvider: () -> Parser<T>
) : Parser<T> {
    actual val parser: Parser<T>
        get() = parserProvider()

    override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<T> =
        parser.tryParse(tokens, fromPosition)
}