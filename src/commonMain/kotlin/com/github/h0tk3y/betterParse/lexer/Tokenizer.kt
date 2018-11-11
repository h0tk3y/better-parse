package com.github.h0tk3y.betterParse.lexer

interface Tokenizer {
    val tokens: List<Token>

    /** Tokenizes the [input] from a [String] into a [TokenizerMatchesSequence]. */
    fun tokenize(input: String): Sequence<TokenMatch>
}