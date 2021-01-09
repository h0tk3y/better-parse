import java.io.File

fun andCodegen(maxN: Int, outputFile: String) {
    fun genericsStr(i: Int) = (1..i).joinToString(prefix = "<", postfix = ">") { "T$it" }

    val resultCode = buildString {
        appendLine("@file:Suppress(")
        appendLine("    \"NO_EXPLICIT_RETURN_TYPE_IN_API_MODE\", // fixme: bug in Kotlin 1.4.21, fixed in 1.4.30")
        appendLine("    \"MoveLambdaOutsideParentheses\", ")
        appendLine("    \"PackageDirectoryMismatch\"")
        appendLine(")")
        appendLine()

        appendLine("package com.github.h0tk3y.betterParse.combinators")
        appendLine("import com.github.h0tk3y.betterParse.utils.*")
        appendLine("import com.github.h0tk3y.betterParse.parser.*")
        appendLine("import kotlin.jvm.JvmName")
        appendLine()

        for (i in 2 until maxN) {
            val generics = genericsStr(i)

            val reifiedNext = (1..i + 1).joinToString { "reified T$it" }
            val casts = (1..i + 1).joinToString { "it[${it - 1}] as T$it" }

            appendLine(
                """
                @JvmName("and$i") public inline infix fun <$reifiedNext>
                    AndCombinator<Tuple$i$generics>.and(p${i + 1}: Parser<T${i + 1}>)
                    // : AndCombinator<Tuple${i + 1}${genericsStr(i + 1)}> = 
                    = AndCombinator(consumersImpl + p${i + 1}, {
                        Tuple${i + 1}($casts)
                    })
                """.trimIndent() + "\n"
            )

            appendLine(
                """
                @JvmName("and$i${"Operator"}") public inline operator fun <$reifiedNext>
                     AndCombinator<Tuple$i$generics>.times(p${i + 1}: Parser<T${i + 1}>) 
                     // : AndCombinator<Tuple${i + 1}${genericsStr(i + 1)}> = 
                     = this and p${i + 1}
                """.trimIndent() + "\n\n"
            )
        }
    }

    File(outputFile).apply {
        parentFile.mkdirs()
        writeText(resultCode)
    }
}