package com.github.h0tk3y.betterParse.lexer

import java.util.regex.Matcher

actual class RegexToken : Token {
    private val pattern: String
    private val regex: Regex
    private val matcher: Matcher

    companion object {
        const val inputStartPrefix = "\\A"
    }

    private fun prependPatternWithInputStart(patternString: String) =
        if (patternString.startsWith(inputStartPrefix))
            patternString.toRegex()
        else
            ("$inputStartPrefix(?:$patternString)").toRegex()

    actual constructor(name: String?, @Language("RegExp", "", "") patternString: String, ignored: Boolean)
            : super(name, ignored) {
        pattern = patternString
        regex = prependPatternWithInputStart(patternString)
        matcher = regex.toPattern().matcher("")
    }

    actual constructor(name: String?, regex: Regex, ignored: Boolean) : this(name, regex.pattern, ignored)

    override fun match(input: CharSequence, fromIndex: Int): Int {
        matcher.reset(input).region(fromIndex, input.length)

        if (!matcher.find()) {
            return 0
        }

        val end = matcher.end()
        return end - fromIndex
    }

    override fun toString() = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}