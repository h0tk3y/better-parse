package com.github.h0tk3y.betterParse.lexer

import java.io.InputStream
import java.util.*
import java.util.regex.Pattern

interface JvmTokenizer : Tokenizer {
    /** Tokenizes the [input] from an [InputStream] into a [TokenizerMatchesSequence]. */
    fun tokenize(input: InputStream): Sequence<TokenMatch>

    /** Tokenizes the [input] from a [Readable] into a [TokenizerMatchesSequence]. */
    fun tokenize(input: Readable): Sequence<TokenMatch>

    /** Tokenizes the [input] from a [Scanner] into a [TokenizerMatchesSequence]. */
    fun tokenize(input: Scanner): Sequence<TokenMatch>
}

/** Tokenizes input character sequences using the [tokens], prioritized by their order in the list,
 * first matched first. */
class DefaultJvmTokenizer(override val tokens: List<Token>) : JvmTokenizer {
    init {
        require(tokens.isNotEmpty()) { "The tokens list should not be empty" }
    }

    private val patterns =
        tokens.filterIsInstance<TokenRegex>().map { it to it.regex.toPattern() }

    private val allInOnePattern = patterns
        .joinToString("|", prefix = "\\G(?:", postfix = ")") { "(${it.second.patternWithEmbeddedFlags()})" }.toPattern()

    private val patternGroupIndices: List<Int> =
        mutableListOf<Int>().apply {
            var groupId = 1 // the zero group is the whole match
            for (p in patterns) {
                add(groupId) // the group for the current pattern
                groupId += p.second.matcher("").groupCount() + 1 // skip all the nested groups in p
            }
        }

    private fun Pattern.patternWithEmbeddedFlags(): String {
        val flags = flags()
        fun hasFlag(flag: Int): Boolean = (flags and flag) == flag

        val flagsString = buildString {
            if (hasFlag(Pattern.CANON_EQ)) throw UnsupportedOperationException(
                "The CANON_EQ regex flag is not supported as it has no embedded form"
            )

            if (hasFlag(Pattern.CASE_INSENSITIVE)) append("i")
            if (hasFlag(Pattern.COMMENTS)) append("x")
            if (hasFlag(Pattern.MULTILINE)) append("m")
            if (hasFlag(Pattern.DOTALL)) append("s")
            if (hasFlag(Pattern.UNICODE_CASE)) append("u")
            if (hasFlag(Pattern.UNIX_LINES)) append("d")
        }

        val flagsPrefix = if (flagsString.isNotEmpty()) "(?$flagsString)" else ""
        val quotePrefix = if (hasFlag(Pattern.LITERAL)) "\\Q" else ""
        val pattern = pattern().let { if (hasFlag(Pattern.COMMENTS)) "$it\n" else it }
        val quoteSuffix = if (hasFlag(Pattern.LITERAL)) "\\E" else ""
        return "$flagsPrefix$quotePrefix$pattern$quoteSuffix"
    }

    /** Tokenizes the [input] from a [String] into a [TokenizerMatchesSequence]. */
    override fun tokenize(input: String) = tokenize(Scanner(input))

    /** Tokenizes the [input] from an [InputStream] into a [TokenizerMatchesSequence]. */
    override fun tokenize(input: InputStream) = tokenize(Scanner(input))

    /** Tokenizes the [input] from a [Readable] into a [TokenizerMatchesSequence]. */
    override fun tokenize(input: Readable) = tokenize(Scanner(input))

    /** Tokenizes the [input] from a [Scanner] into a [TokenizerMatchesSequence]. */
    override fun tokenize(input: Scanner): Sequence<TokenMatch> =
        TokenizerMatchesSequence(object : AbstractIterator<TokenMatch>() {
            var pos = 0
            var row = 1
            var col = 1

            var errorState = false

            override fun computeNext() {
                if (!input.hasNext() || errorState) {
                    done()
                    return
                }

                val matchResult: java.util.regex.MatchResult
                val matchedToken: Token =
                    if (input.findWithinHorizon(allInOnePattern, 0) != null) {
                        matchResult = input.match()
                        tokens[patternGroupIndices.indexOfFirst { matchResult.group(it) != null }]
                    } else {
                        setNext(TokenMatch(noneMatched, input.next(), pos, row, col))
                        errorState = true
                        return
                    }

                val match = matchResult.group()
                val result = TokenMatch(matchedToken, match, pos, row, col)

                pos += match.length
                col += match.length

                val addRows = match.count { it == '\n' }
                row += addRows
                if (addRows > 0) {
                    col = match.length - match.lastIndexOf('\n')
                }

                setNext(result)
            }
        }, this)
}