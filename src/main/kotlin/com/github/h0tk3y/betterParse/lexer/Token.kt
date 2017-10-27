package com.github.h0tk3y.betterParse.lexer

import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.utils.skipOne
import org.intellij.lang.annotations.Language
import org.intellij.lang.annotations.RegExp

/**
 * Represents a basic detectable part of the input, that is detected by its [pattern] and might be [ignored].
 * Parses to [TokenMatch].
 * The [name] only provides additional information.
 */
class Token(
    name: String?,
    @RegExp @Language("RegExp") val pattern: String,
    val ignored: Boolean = false
) : Parser<TokenMatch> {

    var name: String? = name
        internal set

    override fun toString() =
        (if (name != null) "$name ($pattern)" else pattern) +
        if (ignored) " [ignorable]" else ""

    override tailrec fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<TokenMatch> {
        val token = tokens.firstOrNull()
        return when {
            token == null -> UnexpectedEof(this)
            token.type == noneMatched -> NoMatchingToken(token)
            token.type == this -> Parsed(token, tokens.skipOne())
            token.type.ignored -> this.tryParse(tokens.skipOne())
            else -> if (tokens is TokenizerMatchesSequence && this !in tokens.tokenizer.tokens)
                throw IllegalArgumentException("Token $this not in lexer tokens") else
                MismatchedToken(this, token)
        }
    }
}

/** Token type indicating that there was no [Token] found to be matched by a [Lexer]. */
val noneMatched = Token("no token matched", "", false)

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