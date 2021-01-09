package com.github.h0tk3y.betterParse.lexer

import com.github.h0tk3y.betterParse.parser.Parsed

/**
 * Represents a [Parsed] result of a [Token], with the token [type], the [text] that matched the token in the input
 * sequence, the [offset] in the sequence (starting from 0), and [row] and [column] (both starting from 1).
 */
public data class TokenMatch(
    val type: Token,
    val tokenIndex: Int,
    val input: CharSequence,
    val offset: Int,
    val length: Int,
    val row: Int,
    val column: Int
) : Parsed<TokenMatch>() {
    val text: String get() = input.substring(offset, offset + length)

    override val value: TokenMatch
        get() = this
    override val nextPosition: Int
        get() = tokenIndex + 1
    override fun toString(): String = "${type.name}@$nextPosition for \"$text\" at $offset ($row:$column)"
}