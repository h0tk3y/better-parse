package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.parser.Parser

/** A list of [terms] separated by [separators], which is either empty (both `terms` and `separators`) or contains one
 * more term than there are separators. */
class Separated<T, S>(
    val terms: List<T>,
    val separators: List<S>
) {
    init {
        require(terms.size == separators.size + 1 || terms.isEmpty() && separators.isEmpty())
    }

    /** Returns the result of reducing [terms] and [separators] with [function], starting from the left and processing
     * current result (initially, `terms[0]`), `separators[i]` and `terms[i + 1]` at each step for `i` in `0 until terms.size`.
     * @throws [NoSuchElementException] if [terms] are empty */
    fun reduce(function: (T, S, T) -> T): T {
        if (terms.isEmpty()) throw NoSuchElementException()
        var result = terms.first()
        for (i in separators.indices)
            result = function(result, separators[i], terms[i + 1])
        return result
    }

    /** Returns the result of reducing [terms] and [separators] with [function], starting from the right and processing
     * `terms[i]`, `separators[i]` and current result (initially, `terms.last()`) at each step for `i` in `terms.size - 1 downTo 0`.
     * @throws [NoSuchElementException] if [terms] are empty */
    fun reduceRight(function: (T, S, T) -> T): T {
        if (terms.isEmpty()) throw NoSuchElementException()
        var result = terms.last()
        for (i in separators.indices.reversed())
            result = function(terms[i], separators[i], result)
        return result
    }
}

/** Parses a chain of [term]s separated by [separator], also accepting no matches at all if [acceptZero] is true. */
inline fun <reified T, reified S> separated(
    term: Parser<T>,
    separator: Parser<S>,
    acceptZero: Boolean = false
): Parser<Separated<T, S>> {
    val separatedParser = term and (zeroOrMore(separator and term)) map {
        (first, nexts) ->
        Separated(terms = nexts.mapTo(arrayListOf(first)) { (_, b) -> b }, separators = nexts.map { (a, _) -> a })
    }
    return if (acceptZero)
        optional(separatedParser) map { it ?: Separated(terms = listOf(), separators = listOf()) } else
        separatedParser
}

/** Parses a chain of [term]s separated by [separator], also accepting no matches at all if [acceptZero] is true, and returning
 * only matches of [term]. */
inline fun <reified T, reified S> separatedTerms(
    term: Parser<T>,
    separator: Parser<S>,
    acceptZero: Boolean = false
): Parser<List<T>> = separated(term, separator, acceptZero) map { it.terms }

/** Parses a chain of [term]s separated by [operator] and reduces the result with [Separated.reduce]. */
inline fun <reified T, reified S> leftAssociative(
    term: Parser<T>,
    operator: Parser<S>,
    noinline transform: (T, S, T) -> T
): Parser<T> = separated(term, operator) map { it.reduce(transform) }

/** Parses a chain of [term]s separated by [operator] and reduces the result with [Separated.reduceRight]. */
inline fun <reified T, reified S> rightAssociative(
    term: Parser<T>,
    operator: Parser<S>,
    noinline transform: (T, S, T) -> T
): Parser<T> =
    separated(term, operator) map { it.reduceRight(transform) }
