package com.github.h0tk3y.betterParse.lexer

public inline fun token(ignored: Boolean = false, crossinline matcher: (CharSequence, Int) -> Int): Token =
    object : Token(null, ignored) {
        override fun match(input: CharSequence, fromIndex: Int) = matcher(input, fromIndex)
        override fun toString() = "${name ?: ""} {lambda}" + if (ignored) " [ignorable]" else ""
    }

public inline fun token(name: String, ignored: Boolean = false, crossinline matcher: (CharSequence, Int) -> Int): Token =
    object : Token(name, ignored) {
        override fun match(input: CharSequence, fromIndex: Int) = matcher(input, fromIndex)
        override fun toString() = "$name {lambda}" + if (ignored) " [ignorable]" else ""
    }

