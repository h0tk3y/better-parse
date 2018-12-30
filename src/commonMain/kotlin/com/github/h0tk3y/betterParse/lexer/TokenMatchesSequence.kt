package com.github.h0tk3y.betterParse.lexer

/** Stateful producer of tokens that yields [Token]s from some inputs sequence that it is based upon, one by one */
interface TokenProducer {
    fun nextToken(): TokenMatch?
}

class TokenMatchesSequence(
    private val tokenProducer: TokenProducer,
    val tokenizer: Tokenizer,
    private val matches: ArrayList<TokenMatch> = arrayListOf()
) : Sequence<TokenMatch> {

    operator fun get(position: Int): TokenMatch? {
        while (position >= matches.size) {
            val next = tokenProducer.nextToken() ?: return null
            matches.add(next)
        }
        return matches[position]
    }
    
    fun getNotIgnored(position: Int): TokenMatch? {
        // fill until position
        while (position >= matches.size) {
            val next = tokenProducer.nextToken() ?: return null
            matches.add(next)
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

