package com.github.h0tk3y.betterParse.lexer

/**
 * Represents a [Parsed] result of a [Token], with the token [type], the [text] that matched the token in the input
 * sequence, the [position] in the sequence (starting from 0), and [row] and [column] (both starting from 1).
 */
data class TokenMatch(
    val type: Token,
    val input: CharSequence,
    val position: Int,
    val length: Int,
    val row: Int,
    val column: Int
) {
    val text: String get() = input.substring(position, position + length)
    override fun toString(): String = "${type.name} for \"$text\" at $position ($row:$column)"
}