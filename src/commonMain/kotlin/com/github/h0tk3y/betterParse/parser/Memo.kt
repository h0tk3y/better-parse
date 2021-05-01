package com.github.h0tk3y.betterParse.parser

import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence

internal inline class ParseStateMemo<T>(private val map: MutableMap<Int, MemoResult<T>>) {
    operator fun get(atPosition: Int) = map.get(atPosition)
    operator fun set(atPosition: Int, memoResult: MemoResult<T>) = map.put(atPosition, memoResult)

    constructor(): this(hashMapOf())
}

public class ParsingContext {
    internal val memo: MutableMap<Parser<*>, ParseStateMemo<*>> = hashMapOf()
    internal val lrStateStack: MutableList<LrState<*>> = ArrayList(64)
    internal val lrHeads: MutableMap<Int, LrHead<*>> = hashMapOf()
}

internal sealed class MemoResult<T>

private class MemoParseResult<T>(val parseResult: ParseResult<T>) : MemoResult<T>()

internal data class LrHead<T>(
    val recursiveParser: Parser<T>,
    val involvedSet: MutableSet<Parser<*>>,
    val evalSet: MutableSet<Parser<*>>
)

internal object LrParserNotInvolved : ErrorResult()
internal object LrNoResultYet : ErrorResult()

/**
 * The state of handling left recursion – either discovering it or recovering from it.
 *
 * If [head] is null, then the current parse has not encountered left recursion with [parser] yet.
 * Otherwise, the [head] contains the left recursion details, and [seed] parse may contain a partial result of
 *   unwinding the left recursion, which will be used as the result for [parser] in the next attempt.
 *
 * The [LrState]s form a stack, so they point to the previous parser's [LrState] using [next], which is null for the
 * root parser's entry.
 */
internal class LrState<T>(
    val parser: Parser<T>,
    var seed: ParseResult<T>,
    /**
     * The head entry of the encountered left recursion if encountered.
     */
    var head: LrHead<*>?
) : MemoResult<T>()

public abstract class MemoizedParser<out T> : Parser<T> {
    final override fun tryParse(
        tokens: TokenMatchesSequence,
        fromPosition: Int
    ): ParseResult<T> {
        val context = ParsingContext()
        return tryParse(tokens, fromPosition, context)
    }

    @Suppress("UNCHECKED_CAST")
    private fun memoFromContext(context: ParsingContext): ParseStateMemo<T> =
        context.memo.getOrPut(this) { ParseStateMemo<T>() } as ParseStateMemo<T>

    public open fun tryParse(
        tokens: TokenMatchesSequence,
        fromPosition: Int,
        context: ParsingContext
    ): ParseResult<T> {
        val recalled = recallOrEval(fromPosition, tokens, context)
        val memo = memoFromContext(context)

        return when (recalled) {
            null -> {
                val lr = LrState(this, LrNoResultYet, null)
                context.lrStateStack += lr
                memo[fromPosition] = lr
                val result = tryParseImpl(tokens, fromPosition, context)
                memo[fromPosition] = MemoParseResult(result)
                context.lrStateStack.removeLast()

                if (lr.head != null) {
                    // Left recursion detected
                    lr.seed = result
                    lrAnswer(fromPosition, lr, tokens, context)
                } else {
                    memo[fromPosition] = MemoParseResult(result)
                    result
                }
            }
            is LrState -> {
                // Encountered a parser that's already on the stack – remember the current stack
                setupLr(context, recalled)
                LeftRecursionEncountered()
            }
            is MemoParseResult -> recalled.parseResult
        }
    }

    private fun setupLr(context: ParsingContext, lr: LrState<T>) {
        val lrHead = lr.head ?: LrHead(this, mutableSetOf(), mutableSetOf())
            .also { lr.head = it }
        val involved = lrHead.involvedSet

        for (stackIndex in context.lrStateStack.indices.reversed()) {
            val stackElement = context.lrStateStack.get(stackIndex)
            if (stackElement.head == lr.head)
                break
            stackElement.head = lr.head
            involved += stackElement.parser
        }
    }

    private fun recallOrEval(fromPosition: Int, tokens: TokenMatchesSequence, context: ParsingContext): MemoResult<T>? {
        val head = context.lrHeads[fromPosition]
        val memo = memoFromContext(context)

        if (head == null) {
            return memo[fromPosition]
        }
        if (memo[fromPosition] == null && this != head && this !in head.involvedSet) {
            MemoParseResult(LrParserNotInvolved)
        }
        if (this in head.evalSet) {
            head.evalSet.remove(this)
            memo[fromPosition] = MemoParseResult(tryParseImpl(tokens, fromPosition, context))
        }
        return memo[fromPosition]
    }

    private fun lrAnswer(
        fromPosition: Int,
        lr: LrState<T>,
        tokens: TokenMatchesSequence,
        context:
        ParsingContext
    ): ParseResult<T> {
        val head = lr.head!!
        val seed = lr.seed
        return if (head.recursiveParser != this) {
            return seed
        } else {
            memoFromContext(context)[fromPosition] = MemoParseResult(seed)
            if (seed is Parsed)
                growSeed(seed, fromPosition, head, tokens, context)
            else seed
        }
    }

    private fun growSeed(
        initialSeed: Parsed<T>,
        fromPosition: Int,
        lrHead: LrHead<*>,
        tokens: TokenMatchesSequence,
        context: ParsingContext
    ): ParseResult<T> {
        var seed: Parsed<T> = initialSeed
        val memo = memoFromContext(context)

        context.lrHeads[fromPosition] = lrHead

        while (true) {
            lrHead.evalSet.addAll(lrHead.involvedSet)

            val result = tryParseImpl(tokens, fromPosition, context)
            if (result !is Parsed || result.nextPosition <= seed.nextPosition)
                break
            seed = result
            memo[fromPosition] = MemoParseResult(result)
        }

        context.lrHeads.remove(fromPosition)

        return seed
    }

    protected abstract fun tryParseImpl(
        tokens: TokenMatchesSequence,
        fromPosition: Int,
        context: ParsingContext
    ): ParseResult<T>
}