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

    internal var tokenizer: Tokenizer? = null

    abstract fun match(input: CharSequence): Int

    override fun tryParse(tokens: TokenMatchesSequence, position: Int): ParseResult<TokenMatch> {
        val token = tokens.getNotIgnored(position) ?: return UnexpectedEof(this)
        if (token.type == this)
            return token
        if (token.type == noneMatched)
            return NoMatchingToken(token)
        if (tokenizer != tokens.tokenizer)
            throw IllegalArgumentException("Token $this is not valid for a given Tokenizer")
        return MismatchedToken(this, token)
    }
}

/** Token type indicating that there was no [Token] found to be matched by a [Lexer]. */
val noneMatched = TokenRegex("no token matched", "", false)