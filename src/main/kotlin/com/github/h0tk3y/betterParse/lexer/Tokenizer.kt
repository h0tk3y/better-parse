package com.github.h0tk3y.betterParse.lexer

import com.github.h0tk3y.betterParse.utils.CachedSequence
import java.io.InputStream
import java.util.*
import kotlin.coroutines.experimental.buildSequence

internal class TokenizerMatchesSequence(
    iterator: Iterator<TokenMatch>,
    val tokenizer: Tokenizer,
    cache: ArrayList<TokenMatch> = arrayListOf(),
    startAt: Int = 0
) : CachedSequence<TokenMatch>(iterator, cache, startAt)

interface Tokenizer {
    val tokens: List<Token>

    /** Tokenizes the [input] from a [String] into a [TokenizerMatchesSequence]. */
    fun tokenize(input: String): Sequence<TokenMatch>

    /** Tokenizes the [input] from an [InputStream] into a [TokenizerMatchesSequence]. */
    fun tokenize(input: InputStream): Sequence<TokenMatch>

    /** Tokenizes the [input] from a [Readable] into a [TokenizerMatchesSequence]. */
    fun tokenize(input: Readable): Sequence<TokenMatch>

    /** Tokenizes the [input] from a [Scanner] into a [TokenizerMatchesSequence]. */
    fun tokenize(input: Scanner): Sequence<TokenMatch>
}

/** Tokenizes input character sequences using the [tokens], prioritized by their order in the list,
 * first matched first. */
class DefaultTokenizer(override val tokens: List<Token>) : Tokenizer {
    init {
        require(tokens.isNotEmpty()) { "The tokens list should not be empty" }
    }

    val patterns = tokens.map { it to (it.regex?.toPattern() ?: it.pattern.toPattern()) }
    private val allInOnePattern = patterns.joinToString("|", prefix = "\\G") { "(${it.second.pattern()})" }.toPattern()
    private val patternGroupIndices =
        buildSequence {
            var groupId = 1 // the zero group is the whole match
            for (p in patterns) {
                yield(groupId) // the group for the current pattern
                groupId += p.second.matcher("").groupCount() + 1 // skip all the nested groups in p
            }
        }.toList()

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