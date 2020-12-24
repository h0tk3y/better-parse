package com.github.h0tk3y.betterParse.lexer

public actual class RegexToken : Token {
    private val pattern: String
    private val regex: Regex

    /** To ensure that the [regex] will only match its pattern from the index where it is called on with
     * Regex.find(input, startIndex), set the JS RegExp flag 'y', which makes the RegExp 'sticky'.
     * See: https://javascript.info/regexp-sticky */
    private fun preprocessRegex(@Suppress("UNUSED_PARAMETER") regex: Regex) {
        js(
            """
            var r = regex.nativePattern_0;
            regex.nativePattern_0 = new RegExp(r.source, r.flags + (r.sticky ? "" : "y")); 
            """
        )
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

    public override fun match(input: CharSequence, fromIndex: Int): Int =
        regex.find(input, fromIndex)?.range?.let {
            val length = it.last - it.first + 1
            length
        } ?: 0

    public override fun toString(): String = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""
}
