package com.github.h0tk3y.betterParse.lexer

/** Stateful producer of tokens that yields [Token]s from some inputs sequence that it is based upon, one by one */
public interface TokenProducer {
    public fun nextToken(): TokenMatch?
}

public class TokenMatchesSequence(
    private val tokenProducer: TokenProducer,
    public val tokenizer: Tokenizer,
    private val matches: MutableList<TokenMatch> = arrayListOf()
) : Sequence<TokenMatch> {
    private fun ensureReadPosition(position: Int): Boolean {
        while (position >= matches.size) {
            val next = tokenProducer.nextToken() ?: return false
            matches.add(next)
        }

        return true
    }

    public operator fun get(position: Int): TokenMatch? {
        if (!ensureReadPosition(position))
            return null

        return matches[position]
    }

    public fun getNotIgnored(position: Int): TokenMatch? {
        if (!ensureReadPosition(position))
            return null

        var pos = position

        while (true) {
            val value = if (pos < matches.size)
                matches[pos]
            else {
                val next = tokenProducer.nextToken()

                if (next == null)
                    return null
                else {
                    matches.add(next)
                    next
                }
            }

            if (!value.type.ignored)
                return value

            pos++
        }
    }

    public override fun iterator(): Iterator<TokenMatch> = object : AbstractIterator<TokenMatch>() {
        var position = 0
        var noneMatchedAtThisPosition = false

        override fun computeNext() {
            if (noneMatchedAtThisPosition)
                done()

            val nextMatch = get(position) ?: run {
                done()
                return
            }

            setNext(nextMatch)

            if (nextMatch.type == noneMatched)
                noneMatchedAtThisPosition = true

            ++position
        }
    }
}
