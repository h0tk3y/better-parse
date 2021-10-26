package com.github.h0tk3y.betterParse.st

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.ParserReference
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence
import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.parser.ParsedValue

/** Encloses custom logic for transforming a [Parser] to a parser of [SyntaxTree].
 * A correct implementation overrides [liftToSyntaxTree] so that it calls `recurse` for the sub-parsers, if any, and
 * combines them into the parser that it returns. */
public interface LiftToSyntaxTreeTransformer {
    public interface DefaultTransformerReference {
        public fun <T> transform(parser: Parser<T>): Parser<SyntaxTree<T>>
    }

    public fun <T> liftToSyntaxTree(parser: Parser<T>, default: DefaultTransformerReference): Parser<SyntaxTree<T>>
}

/** Options for transforming a [Parser] to a parser of [SyntaxTree].
 * @param retainSkipped - whether the [skip]ped parsers should be present in the syntax tree structure.
 * @param retainSeparators - whether the separators of [separated], [leftAssociative] and [rightAssociative] should be
 * present in the syntax tree structure. */
public data class LiftToSyntaxTreeOptions(
    val retainSkipped: Boolean = false,
    val retainSeparators: Boolean = true
)

/** Converts a [Parser] of [T] to another that parses [SyntaxTree]. The resulting [SyntaxTree]s will have this parser
 * as [SyntaxTree.parser] and the result that this parser is stored in [SyntaxTree.item]. The [liftOptions] are
 * used to determine which parts of the syntax tree should be dropped. The [structureParsers] define the resulting
 * structure of the syntax tree: only the nodes having these parsers are retained (see: [SyntaxTree.flatten]), pass
 * empty set to retain all nodes. */
public fun <T> Parser<T>.liftToSyntaxTreeParser(
    liftOptions: LiftToSyntaxTreeOptions = LiftToSyntaxTreeOptions(),
    structureParsers: Set<Parser<*>>? = null,
    transformer: LiftToSyntaxTreeTransformer? = null
): Parser<SyntaxTree<T>> {
    val astParser = ParserToSyntaxTreeLifter(liftOptions, transformer).lift(this)
    return if (structureParsers == null)
        astParser else
        astParser.flattened(structureParsers)
}

/** Converts a [Grammar] so that its [Grammar.rootParser] parses a [SyntaxTree]. See: [liftToSyntaxTreeParser]. */
public fun <T> Grammar<T>.liftToSyntaxTreeGrammar(
    liftOptions: LiftToSyntaxTreeOptions = LiftToSyntaxTreeOptions(),
    structureParsers: Set<Parser<*>>? = declaredParsers,
    transformer: LiftToSyntaxTreeTransformer? = null
): Grammar<SyntaxTree<T>> = object : Grammar<SyntaxTree<T>>() {
    override val rootParser: Parser<SyntaxTree<T>> = this@liftToSyntaxTreeGrammar.rootParser
        .liftToSyntaxTreeParser(liftOptions, structureParsers, transformer)

    override val tokens: List<Token> get() = this@liftToSyntaxTreeGrammar.tokens
    override val declaredParsers: Set<Parser<Any?>> = this@liftToSyntaxTreeGrammar.declaredParsers
}

private class ParserToSyntaxTreeLifter(
    val liftOptions: LiftToSyntaxTreeOptions,
    val transformer: LiftToSyntaxTreeTransformer?
) {
    @Suppress("UNCHECKED_CAST")
    fun <T> lift(parser: Parser<T>): Parser<SyntaxTree<T>> {
        if (parser in parsersInStack)
            return referenceResultInStack(parser) as Parser<SyntaxTree<T>>

        parsersInStack += parser

        val result = when (parser) {
            is EmptyParser -> emptyASTParser()
            is Token -> liftTokenToAST(parser)
            is MapCombinator<*, *> -> liftMapCombinatorToAST(parser)
            is AndCombinator -> liftAndCombinatorToAST(parser)
            is OrCombinator -> liftOrCombinatorToAST(parser)
            is OptionalCombinator<*> -> liftOptionalCombinatorToAST(parser)
            is RepeatCombinator<*> -> liftRepeatCombinatorToAST(parser)
            is ParserReference<*> -> liftParserReferenceToAST(parser)
            is SeparatedCombinator<*, *> -> liftSeparatedCombinatorToAST(parser)
            else -> {
                transformer?.liftToSyntaxTree(parser, default) ?: throw IllegalArgumentException("Unexpected parser $this. Provide a custom transformer that can lift it.")
            }
        } as Parser<SyntaxTree<T>>

        resultMap[parser] = result

        parsersInStack -= parser

        return result
    }

    inner class DefaultTransformerReference : LiftToSyntaxTreeTransformer.DefaultTransformerReference {
        override fun <T> transform(parser: Parser<T>): Parser<SyntaxTree<T>> = lift(parser)
    }

    private val default = DefaultTransformerReference()

    private fun liftTokenToAST(token: Token): Parser<SyntaxTree<TokenMatch>> {
        return token.map { SyntaxTree(it, listOf(), token, it.offset until (it.offset + it.length)) }
    }

    private fun <T, R> liftMapCombinatorToAST(combinator: MapCombinator<T, R>): Parser<SyntaxTree<R>> {
        val liftedInner = lift(combinator.innerParser)
        return liftedInner.map {
            SyntaxTree(combinator.transform(it.item), listOf(it), combinator, it.range)
        }
    }

    private fun <T> liftOptionalCombinatorToAST(combinator: OptionalCombinator<T>): Parser<SyntaxTree<T?>> {
        return object: Parser<SyntaxTree<T?>> {
            override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<SyntaxTree<T?>> {
                val result = optional(lift(combinator.parser)).tryParse(tokens, fromPosition)
                return when (result) {
                    is ErrorResult -> result
                    is Parsed -> {
                        val inputPosition = tokens[fromPosition]?.offset ?: 0
                        val ast = SyntaxTree(result.value?.item,
                            listOfNotNull(result.value),
                            combinator,
                            result.value?.range ?: inputPosition..inputPosition)
                        ParsedValue(ast, result.nextPosition)
                    }
                }
            }
        }
    }

    private fun <T> liftParserReferenceToAST(combinator: ParserReference<T>): Parser<SyntaxTree<T>> {
        return lift(combinator.parser)
    }

    private fun <T> liftAndCombinatorToAST(combinator: AndCombinator<T>): AndCombinator<SyntaxTree<T>> {
        val liftedConsumers = combinator.consumersImpl.map {
            when (it) {
                is Parser<*> -> lift(it)
                is SkipParser -> lift(it.innerParser)
                else -> throw IllegalArgumentException()
            }
        }
        return AndCombinator(liftedConsumers) { parsedItems ->
            val nonSkippedResults = combinator.nonSkippedIndices.map { parsedItems[it] }
            val originalResult = combinator.transform(nonSkippedResults.map { (it as SyntaxTree<*>).item })
            val start = (parsedItems.first() as SyntaxTree<*>).range.first
            val end = ((parsedItems.lastOrNull { (it as SyntaxTree<*>).range.last != 0 }) as? SyntaxTree<*>)?.range?.last ?: 0
            @Suppress("UNCHECKED_CAST")
            val children = if (liftOptions.retainSkipped)
                parsedItems as List<SyntaxTree<*>> else
                combinator.nonSkippedIndices.map { parsedItems[it] } as List<SyntaxTree<*>>
            return@AndCombinator SyntaxTree(originalResult, children, combinator, start..end)
        }
    }

    private fun <T> liftOrCombinatorToAST(combinator: OrCombinator<T>): Parser<SyntaxTree<T>> {
        val liftedParsers = combinator.parsers.map { lift(it) }
        return OrCombinator(liftedParsers).map { SyntaxTree(it.item, listOf(it), combinator, it.range) }
    }

    private fun <T> liftRepeatCombinatorToAST(combinator: RepeatCombinator<T>): Parser<SyntaxTree<List<T>>> {
        val liftedInner = lift(combinator.parser)
        return RepeatCombinator(liftedInner, combinator.atLeast, combinator.atMost).map {
            val start = it.firstOrNull()?.range?.start ?: 0
            val end = it.lastOrNull { it.range.endInclusive != 0 }?.range?.endInclusive ?: 0
            SyntaxTree(it.map { it.item }, it, combinator, start..end)
        }
    }

    private fun <T, S> liftSeparatedCombinatorToAST(combinator: SeparatedCombinator<T, S>): Parser<SyntaxTree<Separated<T, S>>> {
        val liftedTerm = lift(combinator.termParser)
        val liftedSeparator = lift(combinator.separatorParser)
        return SeparatedCombinator(liftedTerm, liftedSeparator, combinator.acceptZero).map { separated ->
            val item = Separated(separated.terms.map { it.item }, separated.separators.map { it.item })
            val children = when {
                separated.terms.isEmpty() -> emptyList<SyntaxTree<*>>()
                liftOptions.retainSeparators -> listOf(separated.terms.first()) +
                    (separated.separators zip separated.terms.drop(1)).flatMap { (s, t) -> listOf(s, t) }
                else -> separated.terms
            }
            val start = children.firstOrNull()?.range?.start ?: 0
            val end = children.lastOrNull { it.range.last != 0 }?.range?.last ?: 0
            SyntaxTree(item, children, combinator, start..end)
        }
    }

    private fun emptyASTParser() = EmptyParser.map { SyntaxTree(Unit, emptyList(),
        EmptyParser, 0..0) }

    private val resultMap = hashMapOf<Parser<*>, Parser<SyntaxTree<*>>>()

    private fun referenceResultInStack(parser: Parser<*>) = ParserReference { resultMap[parser]!! }

    private val parsersInStack = hashSetOf<Parser<*>>()
}