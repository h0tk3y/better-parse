package com.github.h0tk3y.betterParse.lexer

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.utils.CachedSequence

internal class TokenizerMatchesSequence(
    iterator: Iterator<TokenMatch>,
    val tokenizer: Tokenizer,
    cache: ArrayList<TokenMatch> = arrayListOf(),
    startAt: Int = 0
) : CachedSequence<TokenMatch>(iterator, cache, startAt)