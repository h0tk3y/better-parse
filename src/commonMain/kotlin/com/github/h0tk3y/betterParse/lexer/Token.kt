package com.github.h0tk3y.betterParse.lexer

import com.github.h0tk3y.betterParse.parser.*

@OptionalExpectation
expect annotation class Language(val value: String, val prefix: String, val suffix: String)

/**
 * Represents a basic detectable part of the input that may be [ignored] during parsing.
 * Parses to [TokenMatch].
 * The [name] only provides additional information.
 */
abstract class Token(name: String? = null, val ignored: Boolean) : Parser<TokenMatch> {
    var name = name
        internal set

    abstract fun match(input: CharSequence, fromIndex: Int): Int

    override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<TokenMatch> =
        tryParseImpl(tokens, fromPosition)

    private tailrec fun tryParseImpl(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<TokenMatch> {
        val tokenMatch = tokens[fromPosition] ?: return UnexpectedEof(this)
        return when {
            tokenMatch.type == this -> tokenMatch
            tokenMatch.type == noneMatched -> NoMatchingToken(tokenMatch)
            tokenMatch.type.ignored -> tryParseImpl(tokens, fromPosition + 1)
            else -> MismatchedToken(this, tokenMatch)
        }
    }
}

/** Token type indicating that there was no [Token] found to be matched by a [Lexer]. */
val noneMatched = object : Token("no token matched", false) {
    override fun match(input: CharSequence, fromIndex: Int): Int = 0
}