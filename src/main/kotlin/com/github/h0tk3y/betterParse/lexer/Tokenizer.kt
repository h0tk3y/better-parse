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

    /** Tokenizes the [input] from a [String] into a [TokenizerMatchesSequence]. */
    override fun tokenize(input: String) = tokenize(Scanner(input))

    /** Tokenizes the [input] from an [InputStream] into a [TokenizerMatchesSequence]. */
    override fun tokenize(input: InputStream) = tokenize(Scanner(input))

    /** Tokenizes the [input] from a [Readable] into a [TokenizerMatchesSequence]. */
    override fun tokenize(input: Readable) = tokenize(Scanner(input))

    /** Tokenizes the [input] from a [Scanner] into a [TokenizerMatchesSequence]. */
    override fun tokenize(input: Scanner): Sequence<TokenMatch> = buildSequence {
        input.useDelimiter("")
        var pos = 0
        var row = 1
        var col = 1

        while (input.hasNext()) {
            val matchedToken = patterns.firstOrNull { (_, pattern) ->
                try {
                    input.skip(pattern)
                    true
                } catch (_: NoSuchElementException) {
                    false
                }
            }

            if (matchedToken == null) {
                yield(TokenMatch(noneMatched, input.next(), pos, row, col))
                break
            }

            val match = input.match().group()
            val result = TokenMatch(matchedToken.first, match, pos, row, col)

            pos += match.length
            col += match.length

            val addRows = match.count { it == '\n' }
            row += addRows
            if (addRows > 0) {
                col = match.length - match.lastIndexOf('\n')
            }

            yield(result)
        }
    }.constrainOnce().iterator().let { TokenizerMatchesSequence(it, this) }
}