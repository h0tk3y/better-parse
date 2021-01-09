package com.github.h0tk3y.betterParse.lexer

public interface Tokenizer {
    public val tokens: List<Token>

    /** Tokenizes the [input] from a [String] into a [TokenMatchesSequence]. */
    public fun tokenize(input: String): TokenMatchesSequence
}