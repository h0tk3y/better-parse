package com.github.h0tk3y.betterParse.lexer

import com.github.h0tk3y.betterParse.parser.*

/**
 * Represents a [SuccessResult] result of a [Token], with the token [type], the [text] that matched the token in the input
 * sequence, the [offset] in the sequence (starting from 0), and [row] and [column] (both starting from 1).
 */
data class TokenMatch(
    val type: Token,
    val tokenIndex: Int,
    val input: CharSequence,
    val offset: Int,
    val length: Int,
    val row: Int,
    val column: Int
) : SuccessResult<TokenMatch>() {
    val text: String get() = input.substring(offset, offset + length)

    override val value: TokenMatch
        get() = this
    override val nextTokenIndex: Int
        get() = tokenIndex + 1
    override fun toString(): String = "${type.name}@$nextTokenIndex for \"$text\" at $offset ($row:$column)"
}