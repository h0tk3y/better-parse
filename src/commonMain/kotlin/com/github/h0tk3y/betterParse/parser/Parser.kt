package com.github.h0tk3y.betterParse.parser

import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence

/** A common interface for parsers that can try to consume a part or the whole [TokenMatch] sequence and return one of
 * possible [ParseResult], either [Parsed] or [ErrorResult] */
public interface Parser<out T> {
    public fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<T>
}

public fun <T> Parser<T>.tryParseWithContextIfSupported(
    tokens: TokenMatchesSequence,
    fromPosition: Int,
    context: ParsingContext
): ParseResult<T> = when (this) {
    is MemoizedParser -> tryParse(tokens, fromPosition, context)
    else -> tryParse(tokens, fromPosition)
}

public object EmptyParser : Parser<Unit> {
    override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<Unit> = ParsedValue(Unit, fromPosition)
}

public fun <T> Parser<T>.tryParseToEnd(tokens: TokenMatchesSequence, position: Int): ParseResult<T> {
    val result = tryParse(tokens, position)
    return when (result) {
        is ErrorResult -> result
        is Parsed -> tokens.getNotIgnored(result.nextPosition)?.let {
            UnparsedRemainder(it)
        } ?: result
    }
}

public fun <T> Parser<T>.parse(tokens: TokenMatchesSequence): T = tryParse(tokens, 0).toParsedOrThrow().value

public fun <T> Parser<T>.parseToEnd(tokens: TokenMatchesSequence): T = tryParseToEnd(tokens, 0).toParsedOrThrow().value


/** Represents a result of input sequence parsing by a [Parser] that tried to parse [T]. */
public sealed class ParseResult<out T>

/** Represents a successful parsing result of a [Parser] that produced [value]
 * and left input starting with [nextPosition] unprocessed. */
public abstract class Parsed<out T> : ParseResult<T>() {
    public abstract val value: T
    public abstract val nextPosition: Int
    
    override fun toString(): String = "Parsed($value)"
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Parsed<*>

        if (value != other.value) return false
        if (nextPosition != other.nextPosition) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + nextPosition
        return result
    }
}

internal class ParsedValue<out T>(override val value: T, override val nextPosition : Int) : Parsed<T>()

/** Represents a parse error of a [Parser] that could not successfully parse an input sequence. */
public abstract class ErrorResult : ParseResult<Nothing>() {
    override fun toString(): String = "ErrorResult"
}

/** A [startsWith] token was found where the end of the input sequence was expected during
 * [tryParseToEnd] or [parseToEnd]. */
public data class UnparsedRemainder(val startsWith: TokenMatch) : ErrorResult()

/** A token was [found] where another type of token was [expected]. */
public data class MismatchedToken(val expected: Token, val found: TokenMatch) : ErrorResult()

/** A lexer could not match the input sequence with any token known to it. Contains [tokenMismatch] with special type */
public data class NoMatchingToken(val tokenMismatch: TokenMatch) : ErrorResult()

/** An end of the input sequence was encountered where a token was [expected]. */
public data class UnexpectedEof(val expected: Token) : ErrorResult()

/** A parser tried several alternatives but all resulted into [errors]. */
public data class AlternativesFailure(val errors: List<ErrorResult>) : ErrorResult()

/** The parser got stuck in left recursion. Its rule or the underlying rule did not provide any alternative that
 * makes progress rather than bet back to the left-recursive rule */
public class LeftRecursionEncountered : ErrorResult()

/** Thrown when a [Parser] is forced to parse a sequence with [parseToEnd] or [parse] and fails with an [ErrorResult]. */
public class ParseException(@Suppress("CanBeParameter") public val errorResult: ErrorResult)
    : Exception("Could not parse input: $errorResult")

/** Throws [ParseException] if the receiver [ParseResult] is a [ErrorResult]. Returns the [Parsed] result otherwise. */
public fun <T> ParseResult<T>.toParsedOrThrow(): Parsed<T> = when (this) {
    is Parsed -> this
    is ErrorResult -> throw ParseException(this)
}