package com.github.h0tk3y.betterParse.lexer

class TokenMatchesSequence(
    val tokenProducer: TokenProducer,
    val tokenizer: Tokenizer,
    val matches: ArrayList<TokenMatch> = arrayListOf(),
    val startAt: Int = 0
) : Sequence<TokenMatch> {

    override fun iterator(): Iterator<TokenMatch> {
        return object : AbstractIterator<TokenMatch>() {
            private var pos = startAt

            override fun computeNext() {
                if (pos < matches.size)
                    setNext(matches[pos])
                else {
                    val next = tokenProducer.advance()
                    if (next == null)
                        done()
                    else {
                        setNext(next)
                        matches.add(next)
                        pos++
                    }
                }
            }
        }
    }

    fun toList(): List<TokenMatch> {
        while (true) {
            val next = tokenProducer.advance() ?: return matches.subList(startAt, matches.size)
            matches.add(next)
        }
    }

    inline fun firstOrNull(predicate: (TokenMatch) -> Boolean): TokenMatch? {
        // fill until startAt
        while (startAt >= matches.size) {
            val next = tokenProducer.advance() ?: return null
            matches.add(next)
        }
        
        var pos = startAt
        while (true) {
            val value = if (pos < matches.size)
                matches[pos]
            else {
                val next = tokenProducer.advance()
                if (next == null)
                    return null
                else {
                    matches.add(next)
                    next
                }
            }
            if (predicate(value))
                return value
            pos++
        }
    }

    fun firstOrNull(): TokenMatch? {
        while (startAt >= matches.size) {
            val next = tokenProducer.advance() ?: return null
            matches.add(next)
        }
        return matches[startAt]
    }

    internal fun skipOne() = TokenMatchesSequence(tokenProducer, tokenizer, matches, startAt + 1)
}

