package com.github.h0tk3y.betterParse.lexer

actual class RegexToken : Token {
    private val pattern: String
    private val regex: Regex

    companion object {
        val inputStartPrefix = "\\A"
    }

    actual constructor(name: String?, @Language("RegExp", "", "") patternString: String, ignored: Boolean)
            : super(name, ignored) {
        pattern = patternString
        regex = if (patternString.startsWith(inputStartPrefix))
            patternString.toRegex()
        else
            ("$inputStartPrefix(?:$patternString)").toRegex()
    }

    actual constructor(name: String?, regex: Regex, ignored: Boolean)
            : super(name, ignored) {
        pattern = regex.pattern
        this.regex = regex
    }

    private val relativeInput = object : CharSequence {
        var fromIndex: Int = 0
        var input: CharSequence = ""

        override val length: Int get() = input.length - fromIndex
        override fun get(index: Int): Char = input[index + fromIndex]
        override fun subSequence(startIndex: Int, endIndex: Int) =
            input.subSequence(startIndex + fromIndex, endIndex + fromIndex)

        override fun toString(): String = String(CharArray(length, this::get))  // FIXME
    }

    override fun match(input: CharSequence, fromIndex: Int): Int {
        relativeInput.input = input
        relativeInput.fromIndex = fromIndex
        println("matching $pattern against $relativeInput")

        return regex.find(relativeInput)?.range?.let {
            val length = it.endInclusive - it.start + 1
            length
        } ?: 0
    }

    override fun toString() = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}