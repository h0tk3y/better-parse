package com.github.h0tk3y.betterParse.lexer

class LiteralToken(name: String?, val text: String, ignored: Boolean = false) : Token(name, ignored) {
    override fun match(input: CharSequence) = if (input.startsWith(text)) text.length else 0
    override fun toString() = "${name ?: ""} ($text)" + if (ignored) " [ignorable]" else ""
}

class CharToken(name: String?, val text: Char, ignored: Boolean = false) : Token(name, ignored) {
    override fun match(input: CharSequence) = if (input.isNotEmpty() && input[0] == text) 1 else 0
    override fun toString() = "${name ?: ""} ($text)" + if (ignored) " [ignorable]" else ""
}

fun literalToken(name: String, text: String, ignore: Boolean = false): Token {
    if (text.length == 1)
        return CharToken(name, text[0], ignore)
    return LiteralToken(name, text, ignore)
}

fun literalToken(text: String, ignore: Boolean = false): Token {
    if (text.length == 1)
        return CharToken(null, text[0], ignore)
    return LiteralToken(null, text, ignore)
}