package com.github.h0tk3y.betterParse.lexer

expect fun Regex.patternWithEmbeddedFlags(): String

private fun Regex.countGroups() = "(?:$pattern)?".toRegex().find("")!!.groups.size - 1

/** Tokenizes input character sequences using the [tokens], prioritized by their order in the list,
 * first matched first. */
class DefaultTokenizer(override val tokens: List<Token>) : Tokenizer {
    init {
        require(tokens.isNotEmpty()) { "The tokens list should not be empty" }
    }

    /** Tokenizes the [input] from a [String] into a [TokenMatchesSequence]. */
    override fun tokenize(input: String): TokenMatchesSequence = tokenize(input as CharSequence)

    /** Tokenizes the [input] from a [Scanner] into a [TokenMatchesSequence]. */
    fun tokenize(input: CharSequence) = TokenMatchesSequence(TokenProducer(tokens, input), this)
}

class TokenProducer(val tokens: List<Token>, private val input: CharSequence) {
    private val inputLength = input.length
    private var pos = 0
    private var row = 1
    private var col = 1

    private val relativeInput = object : CharSequence {
        override val length: Int get() = inputLength - pos
        override fun get(index: Int): Char = input[index + pos]
        override fun subSequence(startIndex: Int, endIndex: Int) = input.subSequence(startIndex + pos, endIndex + pos)

        override fun toString(): String = "" // Avoids performance penalty in Matcher calling toString
    }

    private var errorState = false

    fun advance(): TokenMatch? {
        if (relativeInput.isEmpty() || errorState) {
            return null
        }

        for (index in 0 until tokens.size) {
            val token = tokens[index]
            val matchLength = token.match(relativeInput)
            if (matchLength == 0) 
                continue
            
            val result = TokenMatch(token, input, pos, matchLength, row, col)

            for (i in pos until pos + matchLength) {
                if (input[i] == '\n') {
                    row++
                    col = 1
                } else {
                    col++
                }
            }

            pos += matchLength

            return result
        }

        errorState = true
        return TokenMatch(noneMatched, input, pos, inputLength - pos, row, col)
    }

}