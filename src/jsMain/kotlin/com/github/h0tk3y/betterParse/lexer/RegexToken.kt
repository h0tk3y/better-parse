package com.github.h0tk3y.betterParse.lexer

import kotlin.js.RegExp

public actual class RegexToken : Token {
    private val pattern: String
    private val regex: Regex

    /** To ensure that the [regex] will only match its pattern from the index where it is called on with
     * Regex.find(input, startIndex), set the JS RegExp flag 'y', which makes the RegExp 'sticky'.
     * See: https://javascript.info/regexp-sticky */
    private fun preprocessRegex(regex: Regex) {
        val possibleNames = listOf("nativePattern_1", "nativePattern_0", "_nativePattern")
        for(name in possibleNames) {
            val r = regex.asDynamic()[name]
            if(jsTypeOf(r) !== "undefined" && r !== null) {
                val src = r.source as String
                val flags = r.flags as String + if(r.sticky as Boolean) "" else "y"
                regex.asDynamic()[name] = RegExp(src, flags)
                break
            }
        }
    }

    public actual constructor(name: String?, patternString: String, ignored: Boolean)
            : super(name, ignored) {
        pattern = patternString
        regex = pattern.toRegex()
        preprocessRegex(regex)
    }

    public actual constructor(name: String?, regex: Regex, ignored: Boolean)
            : super(name, ignored) {
        pattern = regex.pattern
        this.regex = regex
        preprocessRegex(regex)
    }

    override fun match(input: CharSequence, fromIndex: Int): Int =
        regex.find(input, fromIndex)?.range?.let {
            val length = it.last - it.first + 1
            length
        } ?: 0

    override fun toString(): String = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}