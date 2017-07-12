package com.example

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser

sealed class BooleanExpression

object TRUE : BooleanExpression()
object FALSE : BooleanExpression()
data class Variable(val name: String) : BooleanExpression()
data class Not(val body: BooleanExpression) : BooleanExpression()
data class And(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
data class Or(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
data class Impl(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()

val booleanGrammar = object : Grammar<BooleanExpression>() {
    val tru by token("true")
    val fal by token("false")
    val id by token("\\w+")
    val lpar by token("\\(")
    val rpar by token("\\)")
    val not by token("!")
    val and by token("&")
    val or by token("\\|")
    val impl by token("->")
    val ws by token("\\s+", ignore = true)

    val term: Parser<BooleanExpression> =
        (tru asJust TRUE) or
        (fal asJust FALSE) or
        (id map { Variable(it.text) }) or
        (-not * parser(this::term) map { Not(it) }) or
        (-lpar * parser(this::implChain) * -rpar)

    val andChain = leftAssociative(term, and) { a, _, b -> And(a, b) }
    val orChain = leftAssociative(andChain, or) { a, _, b -> Or(a, b) }
    val implChain = rightAssociative(orChain, impl) { a, _, b -> Impl(a, b) }

    override val rootParser = implChain
}

fun main(args: Array<String>) {
    val expr = "a & (b1 -> c1) | a1 & !b | !(a1 -> a2) -> a"
    println(booleanGrammar.parseToEnd(expr))
}