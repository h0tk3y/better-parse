package com.github.h0tk3y.betterParse.grammar

import com.github.h0tk3y.betterParse.lexer.JvmTokenizer
import com.github.h0tk3y.betterParse.lexer.Tokenizer
import com.github.h0tk3y.betterParse.parser.parseToEnd
import com.github.h0tk3y.betterParse.parser.tryParseToEnd
import java.io.InputStream
import java.util.*

fun <T> Grammar<T>.tryParseToEnd(input: InputStream) = rootParser.tryParseToEnd(tokenizer.tokenize(input))

fun <T> Grammar<T>.tryParseToEnd(input: Readable) = rootParser.tryParseToEnd(tokenizer.tokenize(input))

fun <T> Grammar<T>.tryParseToEnd(input: Scanner) = rootParser.tryParseToEnd(tokenizer.tokenize(input))

fun <T> Grammar<T>.parseToEnd(input: InputStream): T = rootParser.parseToEnd(tokenizer.tokenize(input))

fun <T> Grammar<T>.parseToEnd(input: Readable): T = rootParser.parseToEnd(tokenizer.tokenize(input))

fun <T> Grammar<T>.parseToEnd(input: Scanner): T = rootParser.parseToEnd(tokenizer.tokenize(input))

fun Tokenizer.tokenize(input: InputStream) =
    if (this is JvmTokenizer)
        tokenize(input)
    else
        throw UnsupportedOperationException(
            "This tokenizer cannot process an InputStream. Use an implementation of JvmTokenizer, such as DefaultJvmTokenizer."
        )

fun Tokenizer.tokenize(input: Readable) =
    if (this is JvmTokenizer)
        tokenize(input)
    else
        throw UnsupportedOperationException(
            "This tokenizer cannot process a Readable. Use an implementation of JvmTokenizer, such as DefaultJvmTokenizer."
        )

fun Tokenizer.tokenize(input: Scanner) =
    if (this is JvmTokenizer)
        tokenize(input)
    else
        throw UnsupportedOperationException(
            "This tokenizer cannot process a Scanner. Use an implementation of JvmTokenizer, such as DefaultJvmTokenizer."
        )