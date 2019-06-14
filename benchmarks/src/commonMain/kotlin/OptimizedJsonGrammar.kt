package com.github.h0tk3y.betterParse.benchmark

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*

object OptimizedJsonGrammar : Grammar<Any?>() {
    private fun Char.isLetterOrDigit() =
        (this in 'a'..'z') || (this in 'A'..'Z') || (this in '0'..'9')

    private fun Char.isDigit() = this >= '0' && this <= '9'

    private fun tokenIdent(text: String) = token { it, at ->
        if (!it.startsWith(text, at)) return@token 0
        if (at + text.length > it.length && it[at + text.length].isLetterOrDigit()) return@token 0
        text.length
    }

    private fun tokenNumber() = token { it, at ->
        var index = at
        val maybeSign = it[index]
        val sign = if (maybeSign == '+' || maybeSign == '-') {
            index++
            true
        } else
            false

        val length = it.length
        while (index < length && it[index].isDigit())
            index++

        if (index < length && it[index] == '.') { // decimal
            index++
            while (index < length && it[index].isDigit())
                index++
        }
        if (index == at || (index == at + 1 && sign)) return@token 0
        index - at
    }

    @Suppress("unused")
    private val whiteSpace by token(ignore = true) { it, at ->
        var index = at
        val length = it.length
        while (index < length && it[index].isWhitespace())
            index++
        index - at
    }

    /* the regex "[^\\"]*(\\["nrtbf\\][^\\"]*)*" matches:
     * "               – opening double quote,
     * [^\\"]*         – any number of not escaped characters, nor double quotes
     * (
     *   \\["nrtbf\\]  – backslash followed by special character (\", \n, \r, \\, etc.)
     *   [^\\"]*       – and any number of non-special characters
     * )*              – repeating as a group any number of times
     * "               – closing double quote
     */
    private val stringLiteral by token { it, at ->
        var index = at
        if (it[index++] != '"') return@token 0
        val length = it.length
        while (index < length && it[index] != '"') {
            if (it[index] == '\\') { // quote 
                index++
            }
            index++
        }
        if (index == length) return@token 0 // unclosed string
        index + 1 - at
    }

    private val comma by literalToken(",")
    private val colon by literalToken(":")

    private val openingBrace by literalToken("{")
    private val closingBrace by literalToken("}")

    private val openingBracket by literalToken("[")
    private val closingBracket by literalToken("]")

    private val nullToken by tokenIdent("null")
    private val trueToken by tokenIdent("true")
    private val falseToken by tokenIdent("false")

    private val num by tokenNumber()

    private val jsonNull: Parser<Any?> by nullToken asJust null
    private val jsonBool: Parser<Boolean> by (trueToken asJust true) or (falseToken asJust false)
    private val string: Parser<String> by (stringLiteral use { input.substring(offset + 1, offset + length - 1) })

    private val number: Parser<Double> by num use { text.toDouble() }

    private val jsonPrimitiveValue: Parser<Any?> by jsonNull or jsonBool or string or number

    private val jsonObject: Parser<Map<String, Any?>> by
    (-openingBrace * separated(string * -colon * this, comma, true) * -closingBrace)
        .map { mutableMapOf<String, Any?>().apply { it.terms.forEach { put(it.t1, it.t2) } } }

    private val jsonArray: Parser<List<Any?>> by
    (-openingBracket * separated(this, comma, true) * -closingBracket)
        .map { it.terms }

    override val rootParser by jsonPrimitiveValue or jsonObject or jsonArray
}