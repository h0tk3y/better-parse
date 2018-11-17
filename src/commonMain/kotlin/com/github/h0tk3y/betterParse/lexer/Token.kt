package com.github.h0tk3y.betterParse.lexer

import com.github.h0tk3y.betterParse.parser.*

@OptionalExpectation
expect annotation class Language(val value: String, val prefix: String, val suffix: String)

/**
 * Represents a basic detectable part of the input, that is detected by its [pattern] and might be [ignored].
 * Parses to [TokenMatch].
 * The [name] only provides additional information.
 */
abstract class Token(name: String? = null, val ignored: Boolean) : Parser<TokenMatch> {
    var name = name
        internal set

    abstract fun match(input: CharSequence): Int

    override tailrec fun tryParse(tokens: TokenMatchesSequence, position: Int): ParseResult<TokenMatch> {
        val token = tokens.firstOrNull(position)
        return when {
            token == null -> UnexpectedEof(this)
            token.type == noneMatched -> NoMatchingToken(token)
            token.type == this -> Parsed(token, position + 1)
            token.type.ignored -> tryParse(tokens, position + 1)
            else -> if (this !in tokens.tokenizer.tokens)
                throw IllegalArgumentException("Token $this not in lexer tokens")
            else
                MismatchedToken(this, token)
        }
    }
}

/** Token type indicating that there was no [Token] found to be matched by a [Lexer]. */
val noneMatched = TokenRegex("no token matched", "", false)