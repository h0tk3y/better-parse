package com.github.h0tk3y.betterParse.parser

import com.github.h0tk3y.betterParse.lexer.*

/** A common interface for parsers that can try to consume a part or the whole [TokenMatch] sequence and return one of
 * possible [ParseResult], either [SuccessResult] or [ErrorResult] */
interface Parser<out T> {
    fun tryParse(tokens: TokenMatchesSequence, position: Int): ParseResult<T>
}

object EmptyParser : Parser<Unit> {
    override fun tryParse(tokens: TokenMatchesSequence, position: Int): ParseResult<Unit> = Parsed(Unit, position)
}

fun <T> Parser<T>.tryParseToEnd(tokens: TokenMatchesSequence, position: Int): ParseResult<T> {
    val result = tryParse(tokens, position)
    return when (result) {
        is ErrorResult -> result
        is SuccessResult -> tokens.getNotIgnored(result.nextTokenIndex)?.let {
            UnparsedRemainder(it)
        } ?: result
    }
}

fun <T> Parser<T>.parse(tokens: TokenMatchesSequence): T = tryParse(tokens, 0).toParsedOrThrow().value

fun <T> Parser<T>.parseToEnd(tokens: TokenMatchesSequence): T = tryParseToEnd(tokens, 0).toParsedOrThrow().value


/** Represents a result of input sequence parsing by a [Parser] that tried to parse [T]. */
sealed class ParseResult<out T>


/** Represents a successful parsing result of a [Parser] that produced [value]
 * and left input starting with [nextTokenIndex] unprocessed. */
abstract class SuccessResult<out T>() : ParseResult<T>() {
    abstract val value: T
    abstract val nextTokenIndex: Int
    
    override fun toString(): String = "Parsed($value)"
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SuccessResult<*>

        if (value != other.value) return false
        if (nextTokenIndex != other.nextTokenIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + nextTokenIndex
        return result
    }
}

class Parsed<out T>(override val value: T, override val nextTokenIndex : Int) : SuccessResult<T>()

/** Represents a parse error of a [Parser] that could not successfully parse an input sequence. */
abstract class ErrorResult : ParseResult<Nothing>() {
    override fun toString() = "ErrorResult"
}

/** A [startsWith] token was found where the end of the input sequence was expected during
 * [tryParseToEnd] or [parseToEnd]. */
data class UnparsedRemainder(val startsWith: TokenMatch) : ErrorResult()

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

/** Throws [ParseException] if the receiver [ParseResult] is a [ErrorResult]. Returns the [SuccessResult] result otherwise. */
fun <T> ParseResult<T>.toParsedOrThrow() = when (this) {
    is SuccessResult -> this
    is ErrorResult -> throw ParseException(this)
}