package com.github.h0tk3y.betterParse.lexer

class RegexToken : Token {
    private val pattern: String
    private val regex: Regex

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

fun regexToken(name: String, @Language("RegExp", "", "") pattern: String, ignore: Boolean = false) =
    RegexToken(name, pattern, ignore)

fun regexToken(name: String, pattern: Regex, ignore: Boolean = false) = RegexToken(name, pattern, ignore)
fun regexToken(@Language("RegExp", "", "") pattern: String, ignore: Boolean = false) = RegexToken(null, pattern, ignore)
fun regexToken(pattern: Regex, ignore: Boolean = false) = RegexToken(null, pattern, ignore)

@Deprecated("Use either regexToken or literalToken. This function will be removed soon", ReplaceWith("regexToken"))
fun token(name: String, @Language("RegExp", "", "") pattern: String, ignore: Boolean = false) =
    RegexToken(name, pattern, ignore)

@Deprecated("Use either regexToken or literalToken. This function will be removed soon", ReplaceWith("regexToken"))
fun token(name: String, pattern: Regex, ignore: Boolean = false) = RegexToken(name, pattern, ignore)

@Deprecated("Use either regexToken or literalToken. This function will be removed soon", ReplaceWith("regexToken"))
fun token(@Language("RegExp", "", "") pattern: String, ignore: Boolean = false) = RegexToken(null, pattern, ignore)

@Deprecated("Use either regexToken or literalToken. This function will be removed soon", ReplaceWith("regexToken"))
fun token(pattern: Regex, ignore: Boolean = false) = RegexToken(null, pattern, ignore)

