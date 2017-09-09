import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.st.*
import org.junit.Assert
import org.junit.Test
import kotlin.coroutines.experimental.buildSequence

private sealed class BooleanExpression

private object TRUE : BooleanExpression()
private object FALSE : BooleanExpression()
private data class Variable(val name: String) : BooleanExpression()
private data class Not(val body: BooleanExpression) : BooleanExpression()
private data class And(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
private data class Or(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
private data class Impl(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()

private val booleanGrammar = object : Grammar<BooleanExpression>() {
    val tru by token("true")
    val fal by token("false")
    val id by token("\\w+")
    val lpar by token("\\(")
    val rpar by token("\\)")
    val not by token("!")
    val and by token("&")
    val or by token("\\|")
    val impl by token("->")
    val ws by token("\\s+", ignore = true)

    val term: Parser<BooleanExpression> by
    (tru asJust TRUE) or
        (fal asJust FALSE) or
        (id map { Variable(it.text) }) or
        (-not * parser(this::term) map { Not(it) }) or
        (-lpar * parser(this::implChain) * -rpar)

    val andChain by leftAssociative(term, and) { a, _, b -> And(a, b) }
    val orChain by leftAssociative(andChain, or) { a, _, b -> Or(a, b) }
    val implChain by rightAssociative(orChain, impl) { a, _, b -> Impl(a, b) }

    override val rootParser by implChain
}

private fun SyntaxTree<*>.topDownNodesSequence(): Sequence<SyntaxTree<*>> = buildSequence {
    yield(this@topDownNodesSequence)
    for (c in children) {
        yieldAll(c.topDownNodesSequence())
    }
}

private fun SyntaxTree<*>.toTopDownStrings() = topDownNodesSequence()
    .filter {
        (it.children.isEmpty() || it.children.size > 1)
    }
    .map {
        val item = it.item
        if (item is TokenMatch)
            item.text else
            it.item?.javaClass?.simpleName
    }.toList()

class TestLiftToAst {
    @Test
    fun continuousRange() {
        val astParser = booleanGrammar.liftToSyntaxTreeGrammar(LiftToSyntaxTreeOptions(retainSkipped = true), structureParsers = emptySet())
        val ast = astParser.parseToEnd("a&(b1->c1)|a1&!b|!(a1->a2)->a")
        checkAstContinuousRange(ast)
    }

    @Test
    fun astStructure() {
        val astParser = booleanGrammar.liftToSyntaxTreeGrammar(LiftToSyntaxTreeOptions(retainSkipped = true), structureParsers = emptySet())
        val ast = astParser.parseToEnd("(!(a)->((b)|(!c))->!(!a)&!(d))")
        val types = ast.toTopDownStrings()

        val expected = listOf("Impl", "Impl", "(", "Separated", "Variable", "!", "Variable", "Variable", "(", "a", ")",
            "->", "Or", "Or", "(", "Separated", "Variable", "Variable", "(", "b", ")", "|", "Not", "Not", "(",
            "Variable", "!", "c", ")", ")", "->", "Separated", "Not", "!", "Not", "Not", "(", "Variable", "!", "a", ")",
            "&", "Variable", "!", "Variable", "Variable", "(", "d", ")", ")")
        Assert.assertEquals(expected, types)
    }

    @Test
    fun testDropSkipped() {
        val astParser = booleanGrammar.liftToSyntaxTreeGrammar(LiftToSyntaxTreeOptions(retainSkipped = false), structureParsers = emptySet())
        val ast = astParser.parseToEnd("(!(a)->((b)|(!c))->!(!a)&!(d))")
        val types = ast.toTopDownStrings()

        val expected = listOf("Separated", "a", "->", "Separated", "b", "|", "c", "->", "Separated", "a", "&", "d")
        Assert.assertEquals(expected, types)
    }

    fun checkAstContinuousRange(syntaxTree: SyntaxTree<*>) {
        val first = syntaxTree.range.first
        val last = syntaxTree.range.last
        if (syntaxTree.children.isNotEmpty()) {
            Assert.assertEquals(first, syntaxTree.children.first().range.first)
            Assert.assertEquals(last, syntaxTree.children.last().range.last)
            for ((i, j) in syntaxTree.children.zip(syntaxTree.children.drop(1))) {
                Assert.assertEquals(i.range.last, j.range.first - 1)
            }
            for (c in syntaxTree.children) {
                checkAstContinuousRange(c)
            }
        }
    }

    @Test
    fun testFlattening() {
        val expr = "a & (b1 -> c1) | a1 & !b | !(a1 -> a2) -> a"
        val booleanAstGrammar = booleanGrammar.liftToSyntaxTreeGrammar(LiftToSyntaxTreeOptions(retainSeparators = false))
        val ast = booleanAstGrammar.parseToEnd(expr)
        Assert.assertTrue(ast.topDownNodesSequence().all { it.parser in booleanAstGrammar.declaredParsers })
        Assert.assertEquals(
            listOf("Impl", "Or", "And", "a", "Impl", "b1", "c1", "And", "a1", "b", "Impl", "a1", "a2", "a"),
            ast.toTopDownStrings())
    }

    @Test
    fun testDropSeparators() {
        val expr = "a & (b1 -> c1) | a1 & !b | !(a1 -> a2) -> a"
        val booleanAstGrammar = booleanGrammar.liftToSyntaxTreeGrammar(LiftToSyntaxTreeOptions(retainSkipped = false, retainSeparators = false))
        val ast = booleanAstGrammar.parseToEnd(expr)
        val separatorStrings = setOf("&", "|", "->")
        Assert.assertTrue(ast.toTopDownStrings().none { it in separatorStrings })
    }

    @Test
    fun testCustomTransformer() {
        class ForcedDuplicate<T>(val alternatives: List<Parser<T>>) : Parser<Pair<T, T>> {
            override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<Pair<T, T>> {
                val res = alternatives.asSequence().map { it to it.tryParse(tokens) }.firstOrNull { it.second is Parsed<T> }
                    ?: return object : ErrorResult() {}
                val (parser1, res1) = res
                val res2 = parser1.tryParse(res1.toParsedOrThrow().remainder)
                return when (res2) {
                    is ErrorResult -> res2
                    is Parsed<T> -> Parsed(res1.toParsedOrThrow().value to res2.value, res2.remainder)
                }
            }
        }

        val transformer = object : LiftToSyntaxTreeTransformer {
            @Suppress("UNCHECKED_CAST")
            override fun <T> liftToAst(
                parser: Parser<T>,
                recurse: LiftToSyntaxTreeTransformer.DefaultTransformerReference
            ): Parser<SyntaxTree<T>> {
                if (parser is ForcedDuplicate<*>)
                    return object : Parser<SyntaxTree<T>> {
                        val parsers = parser.alternatives.map { recurse.transform(it) }

                        override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<SyntaxTree<T>> {
                            val res = parsers.asSequence().map { it to it.tryParse(tokens) }.firstOrNull { it.second is Parsed<*> }
                                ?: return object : ErrorResult() {}
                            val (parser1, res1) = res
                            res1 as Parsed<SyntaxTree<*>>
                            val res2 = parser1.tryParse(res1.toParsedOrThrow().remainder)
                            return when (res2) {
                                is ErrorResult -> res2
                                is Parsed<SyntaxTree<*>> -> Parsed(
                                    SyntaxTree(
                                        item = res1.toParsedOrThrow().value.item to res2.value.item,
                                        children = listOf(res1.value, res2.value),
                                        parser = parser,
                                        range = res1.value.range.start..res2.value.range.endInclusive
                                    ) as SyntaxTree<T>,
                                    res2.remainder)
                            }
                        }
                    }
                else throw IllegalArgumentException("Unexpected parser type")
            }
        }

        val parser = ForcedDuplicate(listOf(booleanGrammar.and, booleanGrammar.or, booleanGrammar.impl))

        val lifted = parser.liftToSyntaxTreeParser(
            transformer = transformer,
            structureParsers = booleanGrammar.declaredParsers
        )

        val result = lifted.tryParse(booleanGrammar.lexer.tokenize("||"))
        val value = result.toParsedOrThrow().value

        @Suppress("USELESS_IS_CHECK")
        Assert.assertTrue(value is SyntaxTree<*>)

        Assert.assertTrue(value.children.size == 2 && value.children.all { it.parser === booleanGrammar.or })
    }
} 