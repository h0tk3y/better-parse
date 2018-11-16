package com.github.h0tk3y.betterParse.utils

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.TokenizerMatchesSequence

open class CachedSequence<T> constructor(
        val source: Iterator<T>,
        val cache: ArrayList<T>,
        val startAt: Int
) : Sequence<T> {

    constructor(source: Sequence<T>) : this(source.iterator(), ArrayList(), 0)

    override fun iterator(): Iterator<T> {
        return object : Iterator<T> {
            var pos = startAt

            override fun hasNext() = pos < cache.size || source.hasNext()

            override fun next(): T {
                while (pos >= cache.size)
                    cache.add(source.next())
                return cache[pos++]
            }
        }
    }
}

internal fun Sequence<TokenMatch>.skipOne(): Sequence<TokenMatch> = when (this) {
    is TokenizerMatchesSequence -> TokenizerMatchesSequence(source, tokenizer, cache, startAt + 1)
    is CachedSequence -> CachedSequence(source, cache, startAt + 1)
    else -> drop(1)
}

/**
 * Creates a sequence that caches the once-evaluated items of the original sequence, so that
 * repeated iteration does not lead to evaluating the items again.
 */
fun <T> Sequence<T>.cached(): Sequence<T> = when (this) {
    is CachedSequence -> this
    else -> CachedSequence(this)
}