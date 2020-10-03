package com.github.h0tk3y.betterParse.st

import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.ParserReference
import com.github.h0tk3y.betterParse.parser.Parser

/** Stores the syntactic structure of a [parser] parsing result, with [item] as the result value,
 * [children] storing the same structure of the referenced parsers and [range] displaying the positions
 * in the input sequence. */
public data class SyntaxTree<out T>(
    val item: T,
    val children: List<SyntaxTree<*>>,
    val parser: Parser<T>,
    val range: IntRange
)

/** Returns a [SyntaxTree] that contains only parsers from [structureParsers] in its nodes. The nodes that have other parsers
 * are replaced in their parents by their children that are also flattened in the same way. If the root node is to be
 * replaced, another SyntaxTree is created that contains the resulting nodes as children and the same parser. */
public fun <T> SyntaxTree<T>.flatten(structureParsers: Set<Parser<*>>): SyntaxTree<T> {
    val list = flattenToList(this, structureParsers)
    @Suppress("UNCHECKED_CAST")
    return if (parser == list.singleOrNull()?.parser)
        list.single() as SyntaxTree<T> else
        SyntaxTree(item, list, parser, range)
}

/** Creates another SyntaxTree parser that [flatten]s the result of this parser. */
public fun <T> Parser<SyntaxTree<T>>.flattened(structureParsers: Set<Parser<*>>): Parser<SyntaxTree<T>> =
    map { it.flatten(structureParsers) }

/** Performs the same operation as [flatten], using the parsers defined in [grammar] as `structureParsers`. */
public fun <T> SyntaxTree<T>.flattenForGrammar(grammar: Grammar<*>): SyntaxTree<T> = flatten(grammar.declaredParsers)

/** Performs the same as [flattened], using the parsers defined in [grammar] as `structureParsers`. */
public fun <T> Parser<SyntaxTree<T>>.flattenedForGrammar(grammar: Grammar<*>): Parser<SyntaxTree<T>> =
    map { it.flattenForGrammar(grammar) }

private fun <T> flattenToList(syntaxTree: SyntaxTree<T>, structureParsers: Set<Parser<*>>): List<SyntaxTree<*>> {
    val flattenedChildren = syntaxTree.children.flatMap { flattenToList(it, structureParsers) }
    return if (syntaxTree.parser in structureParsers || syntaxTree.parser is ParserReference && syntaxTree.parser.parser in structureParsers)
        listOf(syntaxTree.copy(children = flattenedChildren)) else
        flattenedChildren
}
