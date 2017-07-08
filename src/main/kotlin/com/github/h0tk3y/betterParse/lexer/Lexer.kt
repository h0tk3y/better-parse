package com.github.h0tk3y.betterParse.lexer

import com.github.h0tk3y.betterParse.utils.CachedSequence
import com.github.h0tk3y.betterParse.utils.cached
import java.io.InputStream
import java.util.*
import kotlin.coroutines.experimental.buildSequence

internal class LexerTokenSequence(
    val tokens: CachedSequence<TokenMatch>,
    val lexer: Lexer
) : Sequence<TokenMatch> by tokens

/** Tokenizes input character sequences using the [tokens], prioritized by their order in the list,
 * first matched first. */
class Lexer(val tokens: List<Token>) {
    init {
        require(tokens.isNotEmpty())
    }

    /** Tokenizes the [input] from a [String] into a [LexerTokenSequence]. */
    fun tokenize(input: String) = tokenize(Scanner(input))

    /** Tokenizes the [input] from an [InputStream] into a [LexerTokenSequence]. */
    fun tokenize(input: InputStream) = tokenize(Scanner(input))

    /** Tokenizes the [input] from a [Readable] into a [LexerTokenSequence]. */
    fun tokenize(input: Readable) = tokenize(Scanner(input))

    /** Tokenizes the [input] from a [Scanner] into a [LexerTokenSequence]. */
    fun tokenize(input: Scanner): Sequence<TokenMatch> = buildSequence {
        input.useDelimiter("")
        var pos = 0
        var row = 1
        var col = 1

        while (input.hasNext()) {
            val matchedToken = tokens.firstOrNull() {
                try {
                    input.skip(it.pattern)
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
            val result = TokenMatch(matchedToken, match, pos, row, col)

            pos += match.length
            col += match.length

            val addRows = match.count { it == '\n' }
            row += addRows
            if (addRows > 0) {
                col = match.length - match.lastIndexOf('\n')
            }

            yield(result)
        }
    }.constrainOnce().cached().let { LexerTokenSequence(it, this) }
}