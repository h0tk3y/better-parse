package com.github.h0tk3y.betterParse.lexer

expect fun Regex.patternWithEmbeddedFlags(): String

private fun Regex.countGroups() = "(?:$pattern)?".toRegex().find("")!!.groups.size - 1

/** Tokenizes input character sequences using the [tokens], prioritized by their order in the list,
 * first matched first. */
class DefaultTokenizer(override val tokens: List<Token>) : Tokenizer {
    init {
        require(tokens.isNotEmpty()) { "The tokens list should not be empty" }
    }

    /** Tokenizes the [input] from a [String] into a [TokenizerMatchesSequence]. */
    override fun tokenize(input: String): Sequence<TokenMatch> = tokenize(input as CharSequence)

    /** Tokenizes the [input] from a [Scanner] into a [TokenizerMatchesSequence]. */
    fun tokenize(input: CharSequence): Sequence<TokenMatch> =
        TokenizerMatchesSequence(object : AbstractIterator<TokenMatch>() {
            var pos = 0
            var row = 1
            var col = 1

            val relativeInput = object : CharSequence {
                override val length: Int get() = input.length - pos
                override fun get(index: Int): Char = input[index + pos]
                override fun subSequence(startIndex: Int, endIndex: Int) =
                    input.subSequence(startIndex + pos, endIndex + pos)

                override fun toString(): String = "" // Avoids performance penalty in Matcher calling toString
            }

            var errorState = false

            override fun computeNext() {
                if (relativeInput.isEmpty() || errorState) {
                    done()
                    return
                }

                for (index in 0 until tokens.size) {
                    val token = tokens[index]
                    val matchLength = token.match(relativeInput) ?: continue
                    val match = input.substring(pos, pos + matchLength)

                    val result = TokenMatch(token, match, pos, row, col)

                    pos += match.length

                    updateRowAndCol(match)

                    setNext(result)
                    return
                }

                setNext(TokenMatch(noneMatched, input.substring(pos), pos, row, col))
                errorState = true
            }

            private fun updateRowAndCol(match: String) {
                var newLine = 0
                while (true) {
                    val nextLine = match.indexOf('\n', newLine)
                    if (nextLine == -1) {
                        col += match.length - newLine
                        break
                    }
                    newLine = nextLine + 1
                    row++
                    col = 1
                }
            }
        }, this)
}