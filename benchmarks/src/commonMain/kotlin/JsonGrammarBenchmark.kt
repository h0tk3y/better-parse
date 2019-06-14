package com.github.h0tk3y.betterParse.benchmark

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.jetbrains.gradle.benchmarks.Benchmark
import org.jetbrains.gradle.benchmarks.Scope
import org.jetbrains.gradle.benchmarks.State

@State(Scope.Benchmark)
open class JsonGrammar {
    @Benchmark
    open fun jsonBetterParseNaive() {
        NaiveJsonGrammar.parseToEnd(jsonSample1K)
    }

    @Benchmark
    open fun jsonBetterParse() {
        OptimizedJsonGrammar.parseToEnd(jsonSample1K)
    }
    
    @Benchmark
    open fun jsonKotlinxDeserializer() {
        Json(JsonConfiguration()).parseJson(jsonSample1K)
    }
}