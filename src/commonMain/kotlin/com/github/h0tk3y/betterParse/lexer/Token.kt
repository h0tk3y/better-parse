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

    override tailrec fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<TokenMatch> {
        val tokenMatch = tokens[fromPosition] ?: return UnexpectedEof(this)
        return when {
            tokenMatch.type == this -> tokenMatch
            tokenMatch.type == noneMatched -> NoMatchingToken(tokenMatch)
            tokenizer != tokens.tokenizer ->
                throw IllegalArgumentException("Token $this is not valid for a given Tokenizer")
            tokenMatch.type.ignored -> tryParse(tokens, fromPosition + 1)
            else -> MismatchedToken(this, tokenMatch)
        }
    }
}

/** Token type indicating that there was no [Token] found to be matched by a [Lexer]. */
val noneMatched = object : Token("no token matched", false) {
    override fun match(input: CharSequence): Int = 0
}