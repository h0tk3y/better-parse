package com.github.h0tk3y.betterParse.lexer

import java.util.regex.Matcher

public actual class RegexToken : Token {
    private val pattern: String
    private val regex: Regex
    private val matcher: Matcher

    public companion object {
        public const val inputStartPrefix: String = "\\A"
    }

    private fun prependPatternWithInputStart(patternString: String, options: Set<RegexOption> = emptySet()): Regex {
        if (RegexOption.LITERAL in options)
            return patternString.toRegex(RegexOption.LITERAL)

        if (patternString.startsWith(inputStartPrefix))
            return patternString.toRegex(options)
        else {
            val needLineBreak = RegexOption.COMMENTS in options && "#" in patternString.split("\n").last()
            return ("$inputStartPrefix(?:$patternString${if (needLineBreak) "\n" else ""})").toRegex(options)
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
        matcher = this.regex.toPattern().matcher("")
    }

    public override fun match(input: CharSequence, fromIndex: Int): Int {
        matcher.reset(input).region(fromIndex, input.length)

        if (!matcher.find())
            return 0

        val end = matcher.end()
        return end - fromIndex
    }

    public override fun toString(): String = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}
