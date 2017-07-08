package com.github.h0tk3y.betterParse.parser

import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch

/** A common interface for parsers that can try to consume a part or the whole [TokenMatch] sequence and return one of
 * possible [ParseResult], either [Parsed] or [ErrorResult] */
interface Parser<out T> {
    fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<T>
}

object EmptyParser : Parser<Unit> {
    override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<Unit> = Parsed(Unit, tokens)
}

fun <T> Parser<T>.tryParseToEnd(tokens: Sequence<TokenMatch>) = tryParse(tokens).let { result ->
    when (result) {
        is ErrorResult -> result
        is Parsed -> result.remainder.firstOrNull()?.let { UnexpectedToken(it) } ?: result
    }
}

fun <T> Parser<T>.parse(tokens: Sequence<TokenMatch>): T = tryParse(tokens).toParsedOrThrow().value

fun <T> Parser<T>.parseToEnd(tokens: Sequence<TokenMatch>): T = tryParseToEnd(tokens).toParsedOrThrow().value


/** Represents a result of input sequence parsing by a [Parser] that tried to parse [T]. */
sealed class ParseResult<out T>


/** Represents a successful parsing result of a [Parser] that produced [value] and left a
 * possibly empty input sequence [remainder] unprocessed.*/
data class Parsed<out T>(val value: T, internal val remainder: Sequence<TokenMatch>) : ParseResult<T>() {
    override fun toString(): String = "Parsed($value)"
}


/** Represents a parse error of a [Parser] that could not successfully parse an input sequence. */
abstract class ErrorResult : ParseResult<Nothing>()

/** A token was [found] where the end of the input sequence was expected while [tryParseToEnd] or [parseToEnd]. */
data class UnexpectedToken(val found: TokenMatch) : ErrorResult()

/** A token was [found] where another type of token was [expected]. */
data class MismatchedToken(val expected: Token, val found: TokenMatch) : ErrorResult()

/** A lexer could not match the input sequence with any token known to it. Contains [tokenMismatch] with special type */
data class NoMatchingToken(val tokenMismatch: TokenMatch) : ErrorResult()

/** An end of the input sequence was encountered where a token was [expected]. */
data class UnexpectedEof(val expected: Token) : ErrorResult()

/** A parser tried several alternatives but all resulted into [errors]. */
data class AlternativesFailure(val errors: List<ErrorResult>) : ErrorResult()


/** Thrown when a [Parser] is forced to parse a sequence with [parseToEnd] or [parse] and fails with an [ErrorResult]. */
class ParseException(val errorResult: ErrorResult) : Exception("Could not parse input: $errorResult")

/** Throws [ParseException] if the receiver [ParseResult] is a [ErrorResult]. Returns the [Parsed] result otherwise. */
fun <T> ParseResult<T>.toParsedOrThrow() = when (this) {
    is Parsed -> this
    is ErrorResult -> throw ParseException(this)
}