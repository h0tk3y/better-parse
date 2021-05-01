package com.github.h0tk3y.betterParse.lexer

import java.util.*
import java.util.regex.Matcher

public actual class RegexToken private constructor(
    name: String?,
    ignored: Boolean,
    private val pattern: String,
    private val regex: Regex
) : Token(name, ignored) {

    private val threadLocalMatcher = object : ThreadLocal<Matcher>() {
        override fun initialValue() = regex.toPattern().matcher("")
    }

    private val matcher: Matcher get() = threadLocalMatcher.get()

    private companion object {
        private const val inputStartPrefix = "\\A"

        private fun prependPatternWithInputStart(patternString: String, options: Set<RegexOption>) =
            if (patternString.startsWith(inputStartPrefix))
                patternString.toRegex(options)
            else {
                val newlineAfterComments = if (RegexOption.COMMENTS in options) "\n" else ""
                val patternToEmbed = if (RegexOption.LITERAL in options) Regex.escape(patternString) else patternString
                ("${inputStartPrefix}(?:$patternToEmbed$newlineAfterComments)").toRegex(options - RegexOption.LITERAL)
            }

    }

    public actual constructor(
        name: String?,
        @Language("RegExp", "", "") patternString: String,
        ignored: Boolean
    ) : this(
        name,
        ignored,
        patternString,
        prependPatternWithInputStart(patternString, emptySet())
    )

    public actual constructor(
        name: String?,
        regex: Regex,
        ignored: Boolean
    ) : this(
        name,
        ignored,
        regex.pattern,
        prependPatternWithInputStart(regex.pattern, regex.options)
    )

    override fun match(input: CharSequence, fromIndex: Int): Int {
        matcher.reset(input).region(fromIndex, input.length)

        if (!matcher.find()) {
            return 0
        }

        val end = matcher.end()
        return end - fromIndex
    }

    override fun toString(): String = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}