package com.github.h0tk3y.betterParse.lexer

expect class RegexToken : Token {
    constructor(name: String?, @Language("RegExp", "", "") patternString: String, ignored: Boolean = false)
    constructor(name: String?, regex: Regex, ignored: Boolean = false)
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

