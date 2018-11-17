package com.github.h0tk3y.betterParse.lexer

class TokenRegex : Token {
    private val pattern: String
    internal val regex: Regex

    companion object {
        const val inputStartPrefix = "\\A"
    }

    constructor(name: String?, @Language("RegExp", "", "") patternString: String, ignored: Boolean = false)
            : super(name, ignored) {
        pattern = patternString
        regex = if (patternString.startsWith(inputStartPrefix))
            patternString.toRegex()
        else
            ("$inputStartPrefix($patternString)").toRegex()
    }

    constructor(name: String?, regex: Regex, ignored: Boolean = false)
            : super(name, ignored) {
        pattern = regex.pattern
        this.regex = regex
    }

    override fun match(input: CharSequence) = regex.find(input)?.range?.let {
        val length = it.endInclusive - it.start + 1
        length
    } ?: 0

    override fun toString() = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}

fun tokenRegex(name: String, @Language("RegExp", "", "") pattern: String, ignore: Boolean = false) =
    TokenRegex(name, pattern, ignore)

fun tokenRegex(name: String, pattern: Regex, ignore: Boolean = false) = TokenRegex(name, pattern, ignore)
fun tokenRegex(@Language("RegExp", "", "") pattern: String, ignore: Boolean = false) = TokenRegex(null, pattern, ignore)
fun tokenRegex(pattern: Regex, ignore: Boolean = false) = TokenRegex(null, pattern, ignore)