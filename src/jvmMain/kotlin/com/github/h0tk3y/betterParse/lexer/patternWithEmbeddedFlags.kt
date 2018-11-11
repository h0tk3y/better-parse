package com.github.h0tk3y.betterParse.lexer

actual fun Regex.patternWithEmbeddedFlags(): String {
    val flags = options
    fun hasFlag(flag: RegexOption): Boolean = flag in flags

    val flagsString = buildString {
        if (hasFlag(RegexOption.CANON_EQ)) throw UnsupportedOperationException(
            "The CANON_EQ regex flag is not supported as it has no embedded form"
        )

        if (hasFlag(RegexOption.IGNORE_CASE)) append("i")
        if (hasFlag(RegexOption.COMMENTS)) append("x")
        if (hasFlag(RegexOption.MULTILINE)) append("m")
        if (hasFlag(RegexOption.DOT_MATCHES_ALL)) append("s")
        if (hasFlag(RegexOption.UNIX_LINES)) append("d")
    }

    val flagsPrefix = if (flagsString.isNotEmpty()) "(?$flagsString)" else ""
    val quotePrefix = if (hasFlag(RegexOption.LITERAL)) "\\Q" else ""
    val pattern = pattern.let { if (hasFlag(RegexOption.COMMENTS)) "$it\n" else it }
    val quoteSuffix = if (hasFlag(RegexOption.LITERAL)) "\\E" else ""
    return "$flagsPrefix$quotePrefix$pattern$quoteSuffix"
}