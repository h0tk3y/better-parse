@file:Suppress(
    "NO_EXPLICIT_RETURN_TYPE_IN_API_MODE", // fixme: bug in Kotlin 1.4.21, fixed in 1.4.30
    "MoveLambdaOutsideParentheses", 
    "PackageDirectoryMismatch"
)

package com.github.h0tk3y.betterParse.combinators
import com.github.h0tk3y.betterParse.utils.*
import com.github.h0tk3y.betterParse.parser.*
import kotlin.jvm.JvmName

@JvmName("and2") public inline infix fun <reified T1, reified T2, reified T3>
    AndCombinator<Tuple2<T1, T2>>.and(p3: Parser<T3>)
    // : AndCombinator<Tuple3<T1, T2, T3>> = 
    = AndCombinator(consumersImpl + p3, {
        Tuple3(it[0] as T1, it[1] as T2, it[2] as T3)
    })

@JvmName("and2Operator") public inline operator fun <reified T1, reified T2, reified T3>
     AndCombinator<Tuple2<T1, T2>>.times(p3: Parser<T3>) 
     // : AndCombinator<Tuple3<T1, T2, T3>> = 
     = this and p3


@JvmName("and3") public inline infix fun <reified T1, reified T2, reified T3, reified T4>
    AndCombinator<Tuple3<T1, T2, T3>>.and(p4: Parser<T4>)
    // : AndCombinator<Tuple4<T1, T2, T3, T4>> = 
    = AndCombinator(consumersImpl + p4, {
        Tuple4(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4)
    })

@JvmName("and3Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4>
     AndCombinator<Tuple3<T1, T2, T3>>.times(p4: Parser<T4>) 
     // : AndCombinator<Tuple4<T1, T2, T3, T4>> = 
     = this and p4


@JvmName("and4") public inline infix fun <reified T1, reified T2, reified T3, reified T4, reified T5>
    AndCombinator<Tuple4<T1, T2, T3, T4>>.and(p5: Parser<T5>)
    // : AndCombinator<Tuple5<T1, T2, T3, T4, T5>> = 
    = AndCombinator(consumersImpl + p5, {
        Tuple5(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5)
    })

@JvmName("and4Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4, reified T5>
     AndCombinator<Tuple4<T1, T2, T3, T4>>.times(p5: Parser<T5>) 
     // : AndCombinator<Tuple5<T1, T2, T3, T4, T5>> = 
     = this and p5


@JvmName("and5") public inline infix fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6>
    AndCombinator<Tuple5<T1, T2, T3, T4, T5>>.and(p6: Parser<T6>)
    // : AndCombinator<Tuple6<T1, T2, T3, T4, T5, T6>> = 
    = AndCombinator(consumersImpl + p6, {
        Tuple6(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6)
    })

@JvmName("and5Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6>
     AndCombinator<Tuple5<T1, T2, T3, T4, T5>>.times(p6: Parser<T6>) 
     // : AndCombinator<Tuple6<T1, T2, T3, T4, T5, T6>> = 
     = this and p6


@JvmName("and6") public inline infix fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7>
    AndCombinator<Tuple6<T1, T2, T3, T4, T5, T6>>.and(p7: Parser<T7>)
    // : AndCombinator<Tuple7<T1, T2, T3, T4, T5, T6, T7>> = 
    = AndCombinator(consumersImpl + p7, {
        Tuple7(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[6] as T7)
    })

@JvmName("and6Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7>
     AndCombinator<Tuple6<T1, T2, T3, T4, T5, T6>>.times(p7: Parser<T7>) 
     // : AndCombinator<Tuple7<T1, T2, T3, T4, T5, T6, T7>> = 
     = this and p7


@JvmName("and7") public inline infix fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8>
    AndCombinator<Tuple7<T1, T2, T3, T4, T5, T6, T7>>.and(p8: Parser<T8>)
    // : AndCombinator<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> = 
    = AndCombinator(consumersImpl + p8, {
        Tuple8(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[6] as T7, it[7] as T8)
    })

@JvmName("and7Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8>
     AndCombinator<Tuple7<T1, T2, T3, T4, T5, T6, T7>>.times(p8: Parser<T8>) 
     // : AndCombinator<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> = 
     = this and p8


@JvmName("and8") public inline infix fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9>
    AndCombinator<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>.and(p9: Parser<T9>)
    // : AndCombinator<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> = 
    = AndCombinator(consumersImpl + p9, {
        Tuple9(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[6] as T7, it[7] as T8, it[8] as T9)
    })

@JvmName("and8Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9>
     AndCombinator<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>.times(p9: Parser<T9>) 
     // : AndCombinator<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> = 
     = this and p9


@JvmName("and9") public inline infix fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10>
    AndCombinator<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>.and(p10: Parser<T10>)
    // : AndCombinator<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>> = 
    = AndCombinator(consumersImpl + p10, {
        Tuple10(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[6] as T7, it[7] as T8, it[8] as T9, it[9] as T10)
    })

@JvmName("and9Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10>
     AndCombinator<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>.times(p10: Parser<T10>) 
     // : AndCombinator<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>> = 
     = this and p10


@JvmName("and10") public inline infix fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10, reified T11>
    AndCombinator<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>>.and(p11: Parser<T11>)
    // : AndCombinator<Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> = 
    = AndCombinator(consumersImpl + p11, {
        Tuple11(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[6] as T7, it[7] as T8, it[8] as T9, it[9] as T10, it[10] as T11)
    })

@JvmName("and10Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10, reified T11>
     AndCombinator<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>>.times(p11: Parser<T11>) 
     // : AndCombinator<Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> = 
     = this and p11


@JvmName("and11") public inline infix fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10, reified T11, reified T12>
    AndCombinator<Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>>.and(p12: Parser<T12>)
    // : AndCombinator<Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> = 
    = AndCombinator(consumersImpl + p12, {
        Tuple12(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[6] as T7, it[7] as T8, it[8] as T9, it[9] as T10, it[10] as T11, it[11] as T12)
    })

@JvmName("and11Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10, reified T11, reified T12>
     AndCombinator<Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>>.times(p12: Parser<T12>) 
     // : AndCombinator<Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> = 
     = this and p12


@JvmName("and12") public inline infix fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10, reified T11, reified T12, reified T13>
    AndCombinator<Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>>.and(p13: Parser<T13>)
    // : AndCombinator<Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> = 
    = AndCombinator(consumersImpl + p13, {
        Tuple13(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[6] as T7, it[7] as T8, it[8] as T9, it[9] as T10, it[10] as T11, it[11] as T12, it[12] as T13)
    })

@JvmName("and12Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10, reified T11, reified T12, reified T13>
     AndCombinator<Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>>.times(p13: Parser<T13>) 
     // : AndCombinator<Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> = 
     = this and p13


@JvmName("and13") public inline infix fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10, reified T11, reified T12, reified T13, reified T14>
    AndCombinator<Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>>.and(p14: Parser<T14>)
    // : AndCombinator<Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> = 
    = AndCombinator(consumersImpl + p14, {
        Tuple14(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[6] as T7, it[7] as T8, it[8] as T9, it[9] as T10, it[10] as T11, it[11] as T12, it[12] as T13, it[13] as T14)
    })

@JvmName("and13Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10, reified T11, reified T12, reified T13, reified T14>
     AndCombinator<Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>>.times(p14: Parser<T14>) 
     // : AndCombinator<Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> = 
     = this and p14


@JvmName("and14") public inline infix fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10, reified T11, reified T12, reified T13, reified T14, reified T15>
    AndCombinator<Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>>.and(p15: Parser<T15>)
    // : AndCombinator<Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> = 
    = AndCombinator(consumersImpl + p15, {
        Tuple15(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[6] as T7, it[7] as T8, it[8] as T9, it[9] as T10, it[10] as T11, it[11] as T12, it[12] as T13, it[13] as T14, it[14] as T15)
    })

@JvmName("and14Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10, reified T11, reified T12, reified T13, reified T14, reified T15>
     AndCombinator<Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>>.times(p15: Parser<T15>) 
     // : AndCombinator<Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> = 
     = this and p15


@JvmName("and15") public inline infix fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10, reified T11, reified T12, reified T13, reified T14, reified T15, reified T16>
    AndCombinator<Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>>.and(p16: Parser<T16>)
    // : AndCombinator<Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>> = 
    = AndCombinator(consumersImpl + p16, {
        Tuple16(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[6] as T7, it[7] as T8, it[8] as T9, it[9] as T10, it[10] as T11, it[11] as T12, it[12] as T13, it[13] as T14, it[14] as T15, it[15] as T16)
    })

@JvmName("and15Operator") public inline operator fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, reified T10, reified T11, reified T12, reified T13, reified T14, reified T15, reified T16>
     AndCombinator<Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>>.times(p16: Parser<T16>) 
     // : AndCombinator<Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>> = 
     = this and p16


