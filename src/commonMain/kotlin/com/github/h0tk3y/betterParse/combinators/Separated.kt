package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence
import com.github.h0tk3y.betterParse.parser.*

@Suppress("UNCHECKED_CAST")
public class SeparatedCombinator<T, S>(
    public val termParser: Parser<T>,
    public val separatorParser: Parser<S>,
    public val acceptZero: Boolean
) : MemoizedParser<Separated<T, S>>() {
    override fun tryParseImpl(
        tokens: TokenMatchesSequence,
        fromPosition: Int,
        context: ParsingContext
    ): ParseResult<Separated<T, S>> {
        val termMatches = mutableListOf<T>()
        val separatorMatches = mutableListOf<S>()

        val first = termParser.tryParseWithContextIfSupported(tokens, fromPosition, context)

        return when (first) {
            is ErrorResult -> if (acceptZero)
                ParsedValue(Separated(emptyList(), emptyList()), fromPosition)
            else
                first

            is Parsed -> {
                termMatches.add(first.value)
                var nextPosition = first.nextPosition
                loop@ while (true) {
                    val separator = separatorParser.tryParseWithContextIfSupported(tokens, nextPosition, context)
                    when (separator) {
                        is ErrorResult -> break@loop
                        is Parsed -> {
                            val nextTerm =
                                termParser.tryParseWithContextIfSupported(tokens, separator.nextPosition, context)
                            when (nextTerm) {
                                is ErrorResult -> break@loop
                                is Parsed -> {
                                    separatorMatches.add(separator.value)
                                    termMatches.add(nextTerm.value)
                                    nextPosition = nextTerm.nextPosition
                                }
                            }
                        }
                    }
                }
                ParsedValue(Separated(termMatches, separatorMatches), nextPosition)
            }
        }
    }
}

/** A list of [terms] separated by [separators], which is either empty (both `terms` and `separators`) or contains one
 * more term than there are separators. */
public class Separated<T, S>(
    public val terms: List<T>,
    public val separators: List<S>
) {
    init {
        require(terms.size == separators.size + 1 || terms.isEmpty() && separators.isEmpty())
    }

    /** Returns the result of reducing [terms] and [separators] with [function], starting from the left and processing
     * current result (initially, `terms[0]`), `separators[i]` and `terms[i + 1]` at each step for `i` in `0 until
     * terms.size`.
     * @throws [NoSuchElementException] if [terms] are empty */
    public fun reduce(function: (T, S, T) -> T): T {
        if (terms.isEmpty()) throw NoSuchElementException()
        var result = terms.first()
        for (i in separators.indices)
            result = function(result, separators[i], terms[i + 1])
        return result
    }

    /** Returns the result of reducing [terms] and [separators] with [function], starting from the right and processing
     * `terms[i]`, `separators[i]` and current result (initially, `terms.last()`) at each step for `i` in `terms.size - 1 downTo 0`.
     * @throws [NoSuchElementException] if [terms] are empty */
    public fun reduceRight(function: (T, S, T) -> T): T {
        if (terms.isEmpty()) throw NoSuchElementException()
        var result = terms.last()
        for (i in separators.indices.reversed())
            result = function(terms[i], separators[i], result)
        return result
    }
}

/** Parses a chain of [term]s separated by [separator], also accepting no matches at all if [acceptZero] is true. */
public inline fun <reified T, reified S> separated(
    term: Parser<T>,
    separator: Parser<S>,
    acceptZero: Boolean = false
): Parser<Separated<T, S>> = SeparatedCombinator(term, separator, acceptZero)

/** Parses a chain of [term]s separated by [separator], also accepting no matches at all if [acceptZero] is true, and returning
 * only matches of [term]. */
public inline fun <reified T, reified S> separatedTerms(
    term: Parser<T>,
    separator: Parser<S>,
    acceptZero: Boolean = false
): Parser<List<T>> = separated(term, separator, acceptZero) map { it.terms }

/** Parses a chain of [term]s separated by [operator] and reduces the result with [Separated.reduce]. */
public inline fun <reified T, reified S> leftAssociative(
    term: Parser<T>,
    operator: Parser<S>,
    noinline transform: (T, S, T) -> T
): Parser<T> = separated(term, operator) map { it.reduce(transform) }

/** Parses a chain of [term]s separated by [operator] and reduces the result with [Separated.reduceRight]. */
public inline fun <reified T, reified S> rightAssociative(
    term: Parser<T>,
    operator: Parser<S>,
    noinline transform: (T, S, T) -> T
): Parser<T> =
    separated(term, operator) map { it.reduceRight(transform) }
