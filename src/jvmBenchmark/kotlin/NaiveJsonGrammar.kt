package com.github.h0tk3y.betterParse.benchmark

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.parser.Parser
import java.awt.SystemColor.text

object NaiveJsonGrammar : Grammar<Any?>() {
    @Suppress("unused")
    private val whiteSpace by regexToken("[\r|\n]|\\s+", ignore = true)

    /* the regex "[^\\"]*(\\["nrtbf\\][^\\"]*)*" matches:
     * "               – opening double quote,
     * [^\\"]*         – any number of not escaped characters, nor double quotes
     * (
     *   \\["nrtbf\\]  – backslash followed by special character (\", \n, \r, \\, etc.)
     *   [^\\"]*       – and any number of non-special characters
     * )*              – repeating as a group any number of times
     * "               – closing double quote
     */
    private val stringLiteral by regexToken("\"[^\\\\\"]*(\\\\[\"nrtbf\\\\][^\\\\\"]*)*\"")

    private val comma by regexToken(",")
    private val colon by regexToken(":")

    private val openingBrace by regexToken("\\{")
    private val closingBrace by regexToken("}")

    private val openingBracket by regexToken("\\[")
    private val closingBracket by regexToken("]")

    private val nullToken by regexToken("\\bnull\\b")
    private val trueToken by regexToken("\\btrue\\b")
    private val falseToken by regexToken("\\bfalse\\b")

    private val num by regexToken("-?[0-9]*(?:\\.[0-9]*)?")

    private val jsonNull: Parser<Any?> by nullToken asJust null
    private val jsonBool: Parser<Boolean> by (trueToken asJust true) or (falseToken asJust false)
    private val string: Parser<String> by (stringLiteral use { text.substring(1, text.lastIndex) })

    private val number: Parser<Double> by
    num use { text.toDouble() }

    private val jsonPrimitiveValue: Parser<Any?> by
    jsonNull or jsonBool or string or number

    private val jsonObject: Parser<Map<String, Any?>> by
    (-openingBrace * separated(string * -colon * this, comma, true) * -closingBrace)
        .map { mutableMapOf<String, Any?>().apply { it.terms.forEach { put(it.t1, it.t2) } } }

    private val jsonArray: Parser<List<Any?>> by
    (-openingBracket * separated(this, comma, true) * -closingBracket)
        .map { it.terms }

    override val rootParser by jsonPrimitiveValue or jsonObject or jsonArray
}