package com.github.h0tk3y.betterParse.lexer

public expect class RegexToken : Token {
    public constructor(name: String?, @Language("RegExp", "", "") patternString: String, ignored: Boolean = false)
    public constructor(name: String?, regex: Regex, ignored: Boolean = false)
}

public fun regexToken(name: String, @Language("RegExp", "", "") pattern: String, ignore: Boolean = false): RegexToken =
    RegexToken(name, pattern, ignore)

public fun regexToken(name: String, pattern: Regex, ignore: Boolean = false): RegexToken =
    RegexToken(name, pattern, ignore)

public fun regexToken(@Language("RegExp", "", "") pattern: String, ignore: Boolean = false): RegexToken =
    RegexToken(null, pattern, ignore)

public fun regexToken(pattern: Regex, ignore: Boolean = false): RegexToken = RegexToken(null, pattern, ignore)

@Deprecated(
    "Use either regexToken or literalToken. This function will be removed soon",
    ReplaceWith("regexToken(pattern, ignore)")
)
public fun token(name: String, @Language("RegExp", "", "") pattern: String, ignore: Boolean = false): RegexToken =
    RegexToken(name, pattern, ignore)

@Deprecated(
    "Use either regexToken or literalToken. This function will be removed soon",
    ReplaceWith("regexToken(pattern, ignore)")
)
public fun token(name: String, pattern: Regex, ignore: Boolean = false): RegexToken = RegexToken(name, pattern, ignore)

@Deprecated(
    "Use either regexToken or literalToken. This function will be removed soon",
    ReplaceWith("regexToken(pattern, ignore)")
)
public fun token(@Language("RegExp", "", "") pattern: String, ignore: Boolean = false): RegexToken =
    RegexToken(null, pattern, ignore)

@Deprecated(
    "Use either regexToken or literalToken. This function will be removed soon",
    ReplaceWith("regexToken(pattern, ignore)")
)
public fun token(pattern: Regex, ignore: Boolean = false): RegexToken = RegexToken(null, pattern, ignore)