package com.github.h0tk3y.betterParse.grammar

import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*

public actual class ParserReference<out T> internal actual constructor(parserProvider: () -> Parser<T>) : Parser<T> {
    public actual val parser by lazy(parserProvider)

    override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<T> =
        parser.tryParse(tokens, fromPosition)
}
