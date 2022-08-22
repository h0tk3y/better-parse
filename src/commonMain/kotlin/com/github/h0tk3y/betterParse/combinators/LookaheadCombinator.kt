package com.github.h0tk3y.betterParse.combinators

import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence
import com.github.h0tk3y.betterParse.parser.*

public class LookaheadCombinator(
    private val lookaheadParser: Parser<*>,
    private val negated: Boolean
) : Parser<Unit> {
    override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<Unit> {
        val res = lookaheadParser.tryParse(tokens, fromPosition)

        // TODO if it's stupid and it works...
        return if (res is Parsed) {
            if (negated) {
                LookaheadFoundInNegativeLookahead
            } else {
                ParsedValue(Unit, fromPosition)
            }
        } else {
            if (negated) {
                ParsedValue(Unit, fromPosition)
            } else {
                @Suppress("UNCHECKED_CAST")
                res as ParseResult<Unit>
            }
        }
    }
}

public object LookaheadFoundInNegativeLookahead : ErrorResult()

// TODO try to write the operator version(s)
public fun not(parser: Parser<*>): LookaheadCombinator = LookaheadCombinator(parser, true)

public fun pos(parser: Parser<*>): LookaheadCombinator = LookaheadCombinator(parser, false)
