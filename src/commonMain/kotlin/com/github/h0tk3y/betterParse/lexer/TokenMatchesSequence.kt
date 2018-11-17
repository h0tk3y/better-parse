package com.github.h0tk3y.betterParse.lexer

class TokenMatchesSequence(
    val tokenProducer: TokenProducer,
    val tokenizer: Tokenizer,
    val matches: ArrayList<TokenMatch> = arrayListOf()
)  {
    
    fun firstOrNull(position: Int = 0): TokenMatch? {
        while (position >= matches.size) {
            val next = tokenProducer.advance() ?: return null
            matches.add(next)
        }
        return matches[position]
    }
    
    inline fun firstOrNull(position: Int = 0, predicate: (TokenMatch) -> Boolean): TokenMatch? {
        // fill until startAt
        while (position >= matches.size) {
            val next = tokenProducer.advance() ?: return null
            matches.add(next)
        }

        var pos = position
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

    fun toList(position: Int = 0): List<TokenMatch> {
        while (true) {
            val next = tokenProducer.advance() ?: return matches.subList(position, matches.size)
            matches.add(next)
        }
    }
}

