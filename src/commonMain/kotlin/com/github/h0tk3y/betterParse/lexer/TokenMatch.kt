package com.github.h0tk3y.betterParse.lexer

/**
 * Represents a [Parsed] result of a [Token], with the token [type], the [text] that matched the token in the input
 * sequence, the [position] in the sequence (starting from 0), and [row] and [column] (both starting from 1).
 */
data class TokenMatch(
    val type: Token,
    val text: String,
    val position: Int,
    val row: Int,
    val column: Int
) {
    val length: Int get() = text.length
    override fun toString(): String = "${type.name} for \"$text\" at $position ($row:$column)"
}