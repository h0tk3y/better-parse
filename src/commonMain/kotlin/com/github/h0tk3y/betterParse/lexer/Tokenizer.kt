package com.github.h0tk3y.betterParse.lexer

interface Tokenizer {
    val tokens: List<Token>

    /** Tokenizes the [input] from a [String] into a [TokenMatchesSequence]. */
    fun tokenize(input: String): TokenMatchesSequence
}