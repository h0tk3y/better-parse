package com.github.h0tk3y.betterParse.lexer

import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.utils.skipOne

@OptionalExpectation
expect annotation class Language(val value: String, val prefix: String, val suffix: String)

/**
 * Represents a basic detectable part of the input, that is detected by its [pattern] and might be [ignored].
 * Parses to [TokenMatch].
 * The [name] only provides additional information.
 */
class Token : Parser<TokenMatch> {
    val pattern: String
    val regex: Regex
    val ignored: Boolean

    var name: String? = null
        internal set

    constructor(name: String?, @Language("RegExp", "", "") patternString: String, ignored: Boolean = false) {
        this.name = name
        this.ignored = ignored
        pattern = patternString
        regex = if (patternString.startsWith('^'))
            patternString.toRegex()
        else
            ("\\A" + patternString).toRegex()
    }

    constructor(name: String?, regex: Regex, ignored: Boolean = false) {
        this.name = name
        this.ignored = ignored
        pattern = regex.pattern
        this.regex = regex
    }

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