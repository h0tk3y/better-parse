package com.github.h0tk3y.betterParse.lexer

import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.utils.skipOne
import java.util.regex.Pattern

/**
 * Represents a basic detectable part of the input, that is detected by its [pattern] and might be [ignored].
 * Parses to [TokenMatch].
 * The [name] only provides additional information.
 */
class Token(
    val name: String,
    val pattern: Pattern,
    val ignored: Boolean
) : Parser<TokenMatch> {

    constructor(name: String, pattern: String, ignored: Boolean = false)
        : this(name, pattern.toPattern(), ignored)

    override fun toString() = "$name ($pattern)" + if (ignored) " [ignorable]" else ""

    override tailrec fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<TokenMatch> {
        val token = tokens.firstOrNull()
        return when {
            token == null -> UnexpectedEof(this)
            token.type == noneMatched -> NoMatchingToken(token)
            token.type == this -> Parsed(token, tokens.skipOne())
            token.type.ignored -> this.tryParse(tokens.skipOne())
            else -> if (tokens is LexerTokenSequence && this !in tokens.lexer.tokens)
                throw IllegalArgumentException("Token $this not in lexer tokens") else
                MismatchedToken(this, token)
        }
    }
}

/** Token type indicating that there was no [Token] found to be matched by a [Lexer]. */
val noneMatched = Token("no token matched", "".toPattern(), false)

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
    override fun toString(): String = "${type.name} for \"$text\" at $position ($row:$column)"
}