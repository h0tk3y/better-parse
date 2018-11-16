package com.github.h0tk3y.betterParse.lexer

class TokenLambda(name: String?, val matcher: (CharSequence) -> Int?, ignored: Boolean = false) : Token(name, ignored) {
    override fun match(input: CharSequence): Int? = matcher(input)

    override fun toString() = "${name?:""} {lambda}" + if (ignored) " [ignorable]" else ""
}

fun token(ignore: Boolean = false, matcher: (CharSequence) -> Int?) = TokenLambda(null, matcher, ignore)
fun token(name: String, ignore: Boolean = false, matcher: (CharSequence) -> Int?) = TokenLambda(name, matcher, ignore)

