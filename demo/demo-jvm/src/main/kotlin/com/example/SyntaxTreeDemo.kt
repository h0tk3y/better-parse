package com.example

import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.st.liftToSyntaxTreeGrammar

fun main() {
    val exprs = listOf("a -> b | !c",
                       "a & !b | (a -> a & b) -> a | b | a & b",
                       "a & !(b -> a | c) | (c -> d) & !(!c -> !d & a)")

    val readExprSequence = generateSequence {
        print("Enter a boolean expression:")
        val result = readLine()
        if (result.isNullOrBlank()) null else result
    }

    (exprs.asSequence() + readExprSequence).forEach { parseAndPrintTree(it); println("\n") }
}

val booleanSyntaxTreeGrammar = BooleanGrammar.liftToSyntaxTreeGrammar()

fun parseAndPrintTree(expr: String) {
    println(expr)

    when (val result = booleanSyntaxTreeGrammar.tryParseToEnd(expr)) {
        is ErrorResult -> println("Could not parse expression: $result")
        is Parsed<SyntaxTree<BooleanExpression>> -> printSyntaxTree(expr, result.value)
    }
}

fun printSyntaxTree(expr: String, syntaxTree: SyntaxTree<*>) {
    var currentLayer: List<SyntaxTree<*>> = listOf(syntaxTree)
    while (currentLayer.isNotEmpty()) {
        val underscores = currentLayer.flatMap { t -> t.range.map { index -> index to charByTree(t) } }.toMap()
        val underscoreStr = expr.indices.map { underscores[it] ?: ' ' }.joinToString("")
        println(underscoreStr)

        currentLayer = currentLayer.flatMap { it.children }
    }
}

fun charByTree(tree: SyntaxTree<*>): Char = with(BooleanGrammar) {
    when (tree.parser) {
        id -> 'i'
        and, andChain -> '&'
        or, orChain -> '|'
        impl, implChain -> '>'
        not, negation -> '!'
        term -> 't'
        bracedExpression -> '('
        else -> ' '
    }
}
