
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.st.*
import kotlin.test.*

private sealed class BooleanExpression

private fun Any.typeName() = when (this) {
    is Separated<*, *> -> "Separated"
    TRUE -> "TRUE"
    FALSE -> "FALSE"
    is Variable -> "Variable"
    is Not -> "Not"
    is And -> "And"
    is Or -> "Or"
    is Impl -> "Impl"
    else -> "???"
}

private object TRUE : BooleanExpression()
private object FALSE : BooleanExpression()
private data class Variable(val name: String) : BooleanExpression()
private data class Not(val body: BooleanExpression) : BooleanExpression()
private data class And(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
private data class Or(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
private data class Impl(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()

private val booleanGrammar = object : Grammar<BooleanExpression>() {
    val tru by tokenRegex("true")
    val fal by tokenRegex("false")
    val id by tokenRegex("\\w+")
    val lpar by tokenRegex("\\(")
    val rpar by tokenRegex("\\)")
    val not by tokenRegex("!")
    val and by tokenRegex("&")
    val or by tokenRegex("\\|")
    val impl by tokenRegex("->")
    val ws by tokenRegex("\\s+", ignore = true)

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

private fun SyntaxTree<*>.topDownNodesSequence(): Sequence<SyntaxTree<*>> = sequence {
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
            it.item?.typeName()
    }.toList()

class TestLiftToAst {
    @Test
    fun continuousRange() {
        val astParser = booleanGrammar.liftToSyntaxTreeGrammar(LiftToSyntaxTreeOptions(retainSkipped = true), structureParsers = null)
        val ast = astParser.parseToEnd("a&(b1->c1)|a1&!b|!(a1->a2)->a")
        checkAstContinuousRange(ast)
    }

    @Test
    fun astStructure() {
        val astParser = booleanGrammar.liftToSyntaxTreeGrammar(LiftToSyntaxTreeOptions(retainSkipped = true), structureParsers = null)
        val ast = astParser.parseToEnd("(!(a)->((b)|(!c))->!(!a)&!(d))")
        val types = ast.toTopDownStrings()

        val expected = listOf("Impl", "Impl", "(", "Separated", "Variable", "!", "Variable", "Variable", "(", "a", ")",
            "->", "Or", "Or", "(", "Separated", "Variable", "Variable", "(", "b", ")", "|", "Not", "Not", "(",
            "Variable", "!", "c", ")", ")", "->", "Separated", "Not", "!", "Not", "Not", "(", "Variable", "!", "a", ")",
            "&", "Variable", "!", "Variable", "Variable", "(", "d", ")", ")")
        assertEquals(expected, types)
    }

    @Test
    fun testDropSkipped() {
        val astParser = booleanGrammar.liftToSyntaxTreeGrammar(LiftToSyntaxTreeOptions(retainSkipped = false), structureParsers = null)
        val ast = astParser.parseToEnd("(!(a)->((b)|(!c))->!(!a)&!(d))")
        val types = ast.toTopDownStrings()

        val expected = listOf("Separated", "a", "->", "Separated", "b", "|", "c", "->", "Separated", "a", "&", "d")
        assertEquals(expected, types)
    }

    fun checkAstContinuousRange(syntaxTree: SyntaxTree<*>) {
        val first = syntaxTree.range.first
        val last = syntaxTree.range.last
        if (syntaxTree.children.isNotEmpty()) {
            assertEquals(first, syntaxTree.children.first().range.first)
            assertEquals(last, syntaxTree.children.last().range.last)
            for ((i, j) in syntaxTree.children.zip(syntaxTree.children.drop(1))) {
                assertEquals(i.range.last, j.range.first - 1)
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
        assertTrue(ast.topDownNodesSequence().all { it.parser in booleanAstGrammar.declaredParsers })
        assertEquals(
            listOf("Impl", "Or", "And", "a", "Impl", "b1", "c1", "And", "a1", "b", "Impl", "a1", "a2", "a"),
            ast.toTopDownStrings())
    }

    @Test
    fun testDropSeparators() {
        val expr = "a & (b1 -> c1) | a1 & !b | !(a1 -> a2) -> a"
        val booleanAstGrammar = booleanGrammar.liftToSyntaxTreeGrammar(LiftToSyntaxTreeOptions(retainSkipped = false, retainSeparators = false))
        val ast = booleanAstGrammar.parseToEnd(expr)
        val separatorStrings = setOf("&", "|", "->")
        assertTrue(ast.toTopDownStrings().none { it in separatorStrings })
    }

    @Test
    fun testCustomTransformer() {
        class ForcedDuplicate<T>(val alternatives: List<Parser<T>>) :
            Parser<Pair<T, T>> {
            override fun tryParse(
                tokens: TokenMatchesSequence,
                position: Int
            ): ParseResult<Pair<T, T>> {
                val res = alternatives.asSequence().map { it to it.tryParse(tokens, position) }.firstOrNull { it.second is SuccessResult<T> }
                    ?: return object : ErrorResult() {}
                val (parser1, res1) = res
                val res2 = parser1.tryParse(tokens, res1.toParsedOrThrow().nextTokenIndex)
                return when (res2) {
                    is ErrorResult -> res2
                    is SuccessResult<T> -> Parsed(
                        res1.toParsedOrThrow().value to res2.value,
                        res2.nextTokenIndex
                    )
                }
            }
        }

        val transformer = object : LiftToSyntaxTreeTransformer {
            @Suppress("UNCHECKED_CAST")
            override fun <T> liftToSyntaxTree(
                parser: Parser<T>,
                default: LiftToSyntaxTreeTransformer.DefaultTransformerReference
            ): Parser<SyntaxTree<T>> {
                if (parser is ForcedDuplicate<*>)
                    return object : Parser<SyntaxTree<T>> {
                        val parsers = parser.alternatives.map { default.transform(it) }

                        override fun tryParse(
                            tokens: TokenMatchesSequence,
                            position: Int
                        ): ParseResult<SyntaxTree<T>> {
                            val res = parsers.asSequence().map { it to it.tryParse(tokens, position) }.firstOrNull { it.second is SuccessResult<*> }
                                ?: return object : ErrorResult() {}
                            val (parser1, res1) = res
                            res1 as SuccessResult<SyntaxTree<*>>
                            val res2 = parser1.tryParse(tokens, res1.toParsedOrThrow().nextTokenIndex)
                            return when (res2) {
                                is ErrorResult -> res2
                                is SuccessResult<SyntaxTree<*>> -> Parsed(
                                    SyntaxTree(
                                        item = res1.toParsedOrThrow().value.item to res2.value.item,
                                        children = listOf(res1.value, res2.value),
                                        parser = parser,
                                        range = res1.value.range.start..res2.value.range.endInclusive
                                    ) as SyntaxTree<T>,
                                    res2.nextTokenIndex
                                )
                            }
                        }
                    }
                else throw IllegalArgumentException("Unexpected parser type")
            }
        }

        val parser = ForcedDuplicate(listOf(booleanGrammar.and, booleanGrammar.or, booleanGrammar.impl))

        val lifted = parser.liftToSyntaxTreeParser(
            structureParsers = booleanGrammar.declaredParsers,
            transformer = transformer
        )

        val result = lifted.tryParse(booleanGrammar.tokenizer.tokenize("||"),0)
        val value = result.toParsedOrThrow().value

        @Suppress("USELESS_IS_CHECK")
        assertTrue(value is SyntaxTree<*>)

        assertTrue(value.children.size == 2 && value.children.all { it.parser === booleanGrammar.or })
    }
} 