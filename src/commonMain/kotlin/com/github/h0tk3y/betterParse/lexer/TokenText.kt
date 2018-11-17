package com.github.h0tk3y.betterParse.lexer

class TokenText(name: String?, val text: String, ignored: Boolean = false) : Token(name, ignored) {
    override fun match(input: CharSequence) = if (input.startsWith(text)) text.length else 0
    override fun toString() = "${name ?: ""} ($text)" + if (ignored) " [ignorable]" else ""
}

class TokenChar(name: String?, val text: Char, ignored: Boolean = false) : Token(name, ignored) {
    override fun match(input: CharSequence) = if (input.isNotEmpty() && input[0] == text) 1 else 0
    override fun toString() = "${name ?: ""} ($text)" + if (ignored) " [ignorable]" else ""
}

fun tokenText(name: String, text: String, ignore: Boolean = false): Token {
    if (text.length == 1)
        return TokenChar(name, text[0], ignore)
    return TokenText(name, text, ignore)
}

fun tokenText(text: String, ignore: Boolean = false): Token {
    if (text.length == 1)
        return TokenChar(null, text[0], ignore)
    return TokenText(null, text, ignore)
}