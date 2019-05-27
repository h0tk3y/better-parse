package com.github.h0tk3y.betterParse.benchmark

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import kotlinx.serialization.json.JsonTreeParser
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 30)
@Measurement(iterations = 30, time = 1, timeUnit = TimeUnit.SECONDS)
open class JsonGrammar {
    @Benchmark
    open fun jsonBetterParseNaive(bh: Blackhole?) {
        val parseToEnd = NaiveJsonGrammar.parseToEnd(jsonSample1K)
        bh?.consume(parseToEnd)
    }

    @Benchmark
    open fun jsonBetterParse(bh: Blackhole?) {
        val parseToEnd = OptimizedJsonGrammar.parseToEnd(jsonSample1K)
        bh?.consume(parseToEnd)
    }
    
    @Benchmark
    open fun kotlinxJsonDeserializer(bh: Blackhole?) {
        val parseToEnd = JsonTreeParser(jsonSample1K).readFully()
        bh?.consume(parseToEnd)
    }
}