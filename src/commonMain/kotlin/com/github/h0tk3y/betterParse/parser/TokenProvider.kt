package com.github.h0tk3y.betterParse.parser

import com.github.h0tk3y.betterParse.lexer.Token

/**
 * In theory this could be a property or function of Parser, however SkipParser must not implement
 * Parser in order to disambiguate infix functions.
 */
public interface TokenProvider {
    public val tokens: List<Token>
}