package com.github.h0tk3y.betterParse.lexer

/** Stateful producer of tokens that yields [Token]s from some inputs sequence that it is based upon, one by one */
interface TokenProducer {
    fun nextToken(): TokenMatch?
}

class TokenMatchesSequence(
    private val tokenProducer: TokenProducer,
    private val matches: ArrayList<TokenMatch> = arrayListOf()
) : Sequence<TokenMatch> {

    private inline fun ensureReadPosition(position: Int): Boolean {
        while (position >= matches.size) {
            val next = tokenProducer.nextToken()
                ?: return false
            matches.add(next)
        }
        return true
    }

    operator fun get(position: Int): TokenMatch? {
        if (!ensureReadPosition(position)) {
            return null
        }
        return matches[position]
    }
    
    fun getNotIgnored(position: Int): TokenMatch? {
        if (!ensureReadPosition(position)) {
            return null
        }

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

    override fun iterator(): Iterator<TokenMatch> =
        object : AbstractIterator<TokenMatch>() {
            var position = 0
            var noneMatchedAtThisPosition = false

            override fun computeNext() {
                if (noneMatchedAtThisPosition) {
                    done()
                }

                val nextMatch = get(position) ?: run { done(); return }
                setNext(nextMatch)
                if (nextMatch.type == noneMatched) {
                    noneMatchedAtThisPosition = true
                }
                ++position
            }
        }
}

