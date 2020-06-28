package com.github.h0tk3y.betterParse.benchmark

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import kotlin.test.Test

class Main {
    val repeat = 1000_000_000

    @Test
    fun testNaive() {
        NaiveJsonGrammar().parseToEnd(jsonSample1K)
    }

    @Test
    fun testOptimized() {
        repeat(repeat) {
            OptimizedJsonGrammar().parseToEnd(jsonSample1K)
        }
    }
}