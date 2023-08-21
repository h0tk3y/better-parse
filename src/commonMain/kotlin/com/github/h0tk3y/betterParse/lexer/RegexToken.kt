package com.github.h0tk3y.betterParse.lexer


public class RegexToken(name: String?, private val pattern: Regex, ignored: Boolean = false) : Token(name = name, ignored = ignored) {

    public constructor(name: String?, pattern: String, ignored: Boolean = false): this(name, pattern.toRegex(), ignored)
    override fun match(input: CharSequence, fromIndex: Int): Int {
        val result = this.pattern.matchAt(input, fromIndex)
        return result?.range?.count() ?: 0
    }
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