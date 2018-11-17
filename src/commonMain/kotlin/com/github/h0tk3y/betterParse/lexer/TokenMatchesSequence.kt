package com.github.h0tk3y.betterParse.lexer

class TokenMatchesSequence(
    private val tokenProducer: TokenProducer,
    val tokenizer: Tokenizer,
    private val matches: ArrayList<TokenMatch> = arrayListOf()
)  {
    
    operator fun get(position: Int): TokenMatch? {
        while (position >= matches.size) {
            val next = tokenProducer.advance() ?: return null
            matches.add(next)
        }
        return matches[position]
    }
    
    fun getNotIgnored(position: Int): TokenMatch? {
        // fill until position
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
            if (!value.type.ignored)
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

