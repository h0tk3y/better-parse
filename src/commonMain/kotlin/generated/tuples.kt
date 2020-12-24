@file:Suppress("PackageDirectoryMismatch", "NO_EXPLICIT_RETURN_TYPE_IN_API_MODE_WARNING")

package com.github.h0tk3y.betterParse.utils

public data class Tuple1<T1>(val t1: T1) : Tuple
public val <T, T1 : T> Tuple1<T1>.components get() = listOf(t1)

public data class Tuple2<T1, T2>(val t1: T1, val t2: T2) : Tuple
public val <T, T1 : T, T2 : T> Tuple2<T1, T2>.components get() = listOf(t1, t2)

public data class Tuple3<T1, T2, T3>(val t1: T1, val t2: T2, val t3: T3) : Tuple
public val <T, T1 : T, T2 : T, T3 : T> Tuple3<T1, T2, T3>.components get() = listOf(t1, t2, t3)

public data class Tuple4<T1, T2, T3, T4>(val t1: T1, val t2: T2, val t3: T3, val t4: T4) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T> Tuple4<T1, T2, T3, T4>.components get() = listOf(t1, t2, t3, t4)

public data class Tuple5<T1, T2, T3, T4, T5>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T> Tuple5<T1, T2, T3, T4, T5>.components get() = listOf(t1, t2, t3, t4, t5)

public data class Tuple6<T1, T2, T3, T4, T5, T6>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T> Tuple6<T1, T2, T3, T4, T5, T6>.components get() = listOf(t1, t2, t3, t4, t5, t6)

public data class Tuple7<T1, T2, T3, T4, T5, T6, T7>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T, T7 : T> Tuple7<T1, T2, T3, T4, T5, T6, T7>.components get() = listOf(t1, t2, t3, t4, t5, t6, t7)

public data class Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7, val t8: T8) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T, T7 : T, T8 : T> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.components get() = listOf(t1, t2, t3, t4, t5, t6, t7, t8)

public data class Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7, val t8: T8, val t9: T9) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T, T7 : T, T8 : T, T9 : T> Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>.components get() = listOf(t1, t2, t3, t4, t5, t6, t7, t8, t9)

public data class Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7, val t8: T8, val t9: T9, val t10: T10) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T, T7 : T, T8 : T, T9 : T, T10 : T> Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>.components get() = listOf(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)

public data class Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7, val t8: T8, val t9: T9, val t10: T10, val t11: T11) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T, T7 : T, T8 : T, T9 : T, T10 : T, T11 : T> Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>.components get() = listOf(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11)

public data class Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7, val t8: T8, val t9: T9, val t10: T10, val t11: T11, val t12: T12) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T, T7 : T, T8 : T, T9 : T, T10 : T, T11 : T, T12 : T> Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>.components get() = listOf(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12)

public data class Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7, val t8: T8, val t9: T9, val t10: T10, val t11: T11, val t12: T12, val t13: T13) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T, T7 : T, T8 : T, T9 : T, T10 : T, T11 : T, T12 : T, T13 : T> Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>.components get() = listOf(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13)

public data class Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7, val t8: T8, val t9: T9, val t10: T10, val t11: T11, val t12: T12, val t13: T13, val t14: T14) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T, T7 : T, T8 : T, T9 : T, T10 : T, T11 : T, T12 : T, T13 : T, T14 : T> Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>.components get() = listOf(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14)

public data class Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7, val t8: T8, val t9: T9, val t10: T10, val t11: T11, val t12: T12, val t13: T13, val t14: T14, val t15: T15) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T, T7 : T, T8 : T, T9 : T, T10 : T, T11 : T, T12 : T, T13 : T, T14 : T, T15 : T> Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>.components get() = listOf(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15)

public data class Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7, val t8: T8, val t9: T9, val t10: T10, val t11: T11, val t12: T12, val t13: T13, val t14: T14, val t15: T15, val t16: T16) : Tuple
public val <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T, T7 : T, T8 : T, T9 : T, T10 : T, T11 : T, T12 : T, T13 : T, T14 : T, T15 : T, T16 : T> Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>.components get() = listOf(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16)

