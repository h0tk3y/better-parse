package com.example

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.Parser

sealed class BooleanExpression

object TRUE : BooleanExpression()
object FALSE : BooleanExpression()
data class Variable(val name: String) : BooleanExpression()
data class Not(val body: BooleanExpression) : BooleanExpression()
data class And(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
data class Or(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
data class Impl(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()

object BooleanGrammar : Grammar<BooleanExpression>() {
    val tru by tokenText("true")
    val fal by tokenText("false")
    val id by tokenRegex("\\w+")
    val lpar by tokenText("(")
    val rpar by tokenText(")")
    val not by tokenText("!")
    val and by tokenText("&")
    val or by tokenText("|")
    val impl by tokenText("->")
    val ws by tokenRegex("\\s+", ignore = true)

    val negation by -not * parser(this::term) map { Not(it) }
    val bracedExpression by -lpar * parser(this::implChain) * -rpar

    val term: Parser<BooleanExpression> by
        (tru asJust TRUE) or
        (fal asJust FALSE) or
        (id map { Variable(it.text) }) or
        negation or
        bracedExpression

    val andChain by leftAssociative(term, and) { a, _, b -> And(a, b) }
    val orChain by leftAssociative(andChain, or) { a, _, b -> Or(a, b) }
    val implChain by rightAssociative(orChain, impl) { a, _, b -> Impl(a, b) }

    override val rootParser by implChain
}

fun main(args: Array<String>) {
    val expr = "a & (b1 -> c1) | a1 & !b | !(a1 -> a2) -> a"
    println(BooleanGrammar.parseToEnd(expr))
}