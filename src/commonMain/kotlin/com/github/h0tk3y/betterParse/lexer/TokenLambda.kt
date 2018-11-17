package com.github.h0tk3y.betterParse.lexer

inline fun token(ignore: Boolean = false, crossinline matcher: (CharSequence) -> Int): Token {
    return object : Token(null, ignore) {
        override fun match(input: CharSequence) = matcher(input)
        override fun toString() = "${name ?: ""} {lambda}" + if (ignore) " [ignorable]" else ""
    }
}

inline fun token(name: String, ignore: Boolean = false, crossinline matcher: (CharSequence) -> Int): Token {
    return object : Token(name, ignore) {
        override fun match(input: CharSequence) = matcher(input)
        override fun toString() = "${name} {lambda}" + if (ignore) " [ignorable]" else ""
    }
}

