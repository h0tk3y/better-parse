package com.example

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser

class ArithmeticsEvaluator : Grammar<Int>() {
    val num by token("-?\\d+")
    val lpar by token("\\(")
    val rpar by token("\\)")
    val mul by token("\\*")
    val pow by token("\\^")
    val div by token("/")
    val minus by token("-")
    val plus by token("\\+")
    val ws by token("\\s+", ignore = true)

    val number by num use { text.toInt() }
    val term: Parser<Int> by number or
        (skip(minus) and parser(this::term) map { -it }) or
        (skip(lpar) and parser(this::rootParser) and skip(rpar))

    val powChain by leftAssociative(term, pow) { a, _, b -> Math.pow(a.toDouble(), b.toDouble()).toInt() }

    val divMulChain by leftAssociative(powChain, div or mul use { type }) { a, op, b ->
        if (op == div) a / b else a * b
    }

    val subSumChain by leftAssociative(divMulChain, plus or minus use { type }) { a, op, b ->
        if (op == plus) a + b else a - b
    }

    override val rootParser: Parser<Int> by subSumChain
}

fun main(args: Array<String>) {
    val expr = "1 + 2 * (3 - 1^1) - 2^2^2 * (1 + 1)"
    val result = ArithmeticsEvaluator().parseToEnd(expr)
    println(result)
}