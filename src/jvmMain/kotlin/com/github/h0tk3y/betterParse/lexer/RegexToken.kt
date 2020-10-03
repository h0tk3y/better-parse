package com.github.h0tk3y.betterParse.lexer

import java.util.regex.Matcher

public actual class RegexToken : Token {
    private val pattern: String
    private val regex: Regex
    private val matcher: Matcher

    companion object {
        const val inputStartPrefix = "\\A"

        private fun prependPatternWithInputStart(patternString: String, options: Set<RegexOption> = emptySet()): Regex =
            if (patternString.startsWith(inputStartPrefix))
                patternString.toRegex(options)
            else {
                val lines = patternString.split("\n")

                if (lines.size > 1)
                    ("$inputStartPrefix(?:$patternString${if (lines.last().contains("#")) "\n)" else ")"}")
                        .toRegex(options)
                else
                    ("$inputStartPrefix(?:$patternString)").toRegex(options)
            }
    }

    public actual constructor(name: String?, @Language("RegExp", "", "") patternString: String, ignored: Boolean)
            : super(name, ignored) {
        pattern = patternString
        regex = prependPatternWithInputStart(patternString)
        matcher = regex.toPattern().matcher("")
    }

    public actual constructor(name: String?, regex: Regex, ignored: Boolean)
            : super(name, ignored) {
        pattern = regex.pattern
        this.regex = prependPatternWithInputStart(pattern, regex.options)
        println(this.regex)
        matcher = this.regex.toPattern().matcher("")
    }

    override fun match(input: CharSequence, fromIndex: Int): Int {
        matcher.reset(input).region(fromIndex, input.length)

        if (!matcher.find())
            return 0

        val end = matcher.end()
        return end - fromIndex
    }

    override fun toString() = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}