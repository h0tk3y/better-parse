package com.github.h0tk3y.betterParse.lexer

import com.github.h0tk3y.betterParse.parser.Parsed

/**
 * Represents a [Parsed] result of a [Token], with the token [type], the [text] that matched the token in the input
 * sequence, the [offset] in the sequence (starting from 0), and [row] and [column] (both starting from 1).
 */
public data class TokenMatch(
    public val type: Token,
    public val tokenIndex: Int,
    public val input: CharSequence,
    public val offset: Int,
    public val length: Int,
    public val row: Int,
    public val column: Int
) : Parsed<TokenMatch>() {
    public val text: String
        get() = input.substring(offset, offset + length)

    public override val value: TokenMatch
        get() = this

    public override val nextPosition: Int
        get() = tokenIndex + 1

    public override fun toString(): String = "${type.name}@$nextPosition for \"$text\" at $offset ($row:$column)"
}
