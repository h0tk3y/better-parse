package com.github.h0tk3y.betterParse.lexer

actual fun Regex.patternWithEmbeddedFlags(): String {
    val flags = options
    fun hasFlag(flag: RegexOption): Boolean = flag in flags

    val flagsString = buildString {
        if (hasFlag(RegexOption.IGNORE_CASE)) append("i")
        if (hasFlag(RegexOption.MULTILINE)) append("m")
    }

    val flagsPrefix = if (flagsString.isNotEmpty()) "(?$flagsString)" else ""
    return "$flagsPrefix$pattern"
}