package com.github.h0tk3y.betterParse.lexer

public class LiteralToken(name: String?, public val text: String, ignored: Boolean = false) : Token(name, ignored) {
    override fun match(input: CharSequence, fromIndex: Int): Int = if (input.startsWith(text, fromIndex)) text.length else 0
    override fun toString(): String = "${name ?: ""} ($text)" + if (ignored) " [ignorable]" else ""
}

public class CharToken(name: String?, public val text: Char, ignored: Boolean = false) : Token(name, ignored) {
    override fun match(input: CharSequence, fromIndex: Int): Int = if (input.isNotEmpty() && input[fromIndex] == text) 1 else 0
    override fun toString(): String = "${name ?: ""} ($text)" + if (ignored) " [ignorable]" else ""
}

/** Matches any character in [chars]--RegexToken is too greedy */
public class MultiCharToken(name: String?, public val chars: Set<Char>, ignored: Boolean = false) : Token(name, ignored) {
    override fun match(input: CharSequence, fromIndex: Int): Int {
        return if (input.isNotEmpty() && chars.contains(input[fromIndex])) 1 else 0
    }
    override fun toString(): String = "${name ?: ""} [$chars]" + if (ignored) " [ignorable]" else ""
}

public fun literalToken(name: String, text: String, ignore: Boolean = false): Token {
    if (text.length == 1)
        return CharToken(name, text[0], ignore)
    return LiteralToken(name, text, ignore)
}

public fun literalToken(text: String, ignore: Boolean = false): Token {
    if (text.length == 1)
        return CharToken(null, text[0], ignore)
    return LiteralToken(null, text, ignore)
}