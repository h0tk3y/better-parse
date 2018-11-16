package com.github.h0tk3y.betterParse.lexer

expect fun Regex.patternWithEmbeddedFlags(): String

private fun Regex.countGroups() = "(?:$pattern)?".toRegex().find("")!!.groups.size - 1

/** Tokenizes input character sequences using the [tokens], prioritized by their order in the list,
 * first matched first. */
class DefaultTokenizer(override val tokens: List<Token>) : Tokenizer {
    init {
        require(tokens.isNotEmpty()) { "The tokens list should not be empty" }
    }

    val patterns =
        tokens.map { it to (it.regex ?: it.pattern.toRegex()) }

    val allInOnePattern =
        patterns
            .joinToString("|", prefix = "(?:", postfix = ")") { "(${it.second.patternWithEmbeddedFlags()})" }
            .toRegex()

    private val patternGroupIndices =
        mutableListOf<Int>().apply {
            var groupId = 1 // the zero group is the whole match
            for (p in patterns) {
                add(groupId) // the group for the current pattern
                groupId += p.second.countGroups() + 1 // skip all the nested groups in p
            }
        } as List<Int>

    /** Tokenizes the [input] from a [String] into a [TokenizerMatchesSequence]. */
    override fun tokenize(input: String): Sequence<TokenMatch> = tokenize(input as CharSequence)

    /** Tokenizes the [input] from a [Scanner] into a [TokenizerMatchesSequence]. */
    fun tokenize(input: CharSequence): Sequence<TokenMatch> =
        TokenizerMatchesSequence(object : AbstractIterator<TokenMatch>() {
            var pos = 0
            var row = 1
            var col = 1

            var errorState = false

            override fun computeNext() {
                if (pos >= input.length || errorState) {
                    done()
                    return
                }

                val matchResult = allInOnePattern.find(input, pos)
                val matchedToken: Token =
                    if (matchResult != null && matchResult.range.first == pos) {
                        tokens[patternGroupIndices.indexOfFirst { matchResult.groups[it] != null }]
                    } else {
                        setNext(TokenMatch(noneMatched, input.substring(pos), pos, row, col))
                        errorState = true
                        return
                    }

                val match = matchResult.groupValues[0]
                val result = TokenMatch(matchedToken, match, pos, row, col)

                pos += match.length
                updateRowAndCol(match)
                setNext(result)
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