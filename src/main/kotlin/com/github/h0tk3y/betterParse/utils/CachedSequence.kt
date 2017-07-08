package com.github.h0tk3y.betterParse.utils

import java.util.*

internal class CachedSequence<T> constructor(
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

internal fun <T> Sequence<T>.skipOne() = when (this) {
    is CachedSequence<T> -> CachedSequence(source, cache, startAt + 1)
    else -> drop(1)
}

internal fun <T> Sequence<T>.cached() = when (this) {
    is CachedSequence<T> -> this
    else -> CachedSequence(this)
}