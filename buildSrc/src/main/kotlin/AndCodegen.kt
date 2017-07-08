import java.io.File

fun andCodegen(maxN: Int, outputFile: String) {
    fun genericsStr(i: Int) = (1..i).joinToString(prefix = "<", postfix = ">") { "T$it" }

    val resultCode = buildString {
        appendln("package com.github.h0tk3y.betterParse.combinators")
        appendln("import com.github.h0tk3y.betterParse.utils.*")
        appendln("import com.github.h0tk3y.betterParse.parser.*")
        appendln()

        for (i in 2 until maxN) {
            val generics = genericsStr(i)

            val reifiedNext = (1..i + 1).joinToString { "reified T$it" }
            val casts = (1..i + 1).joinToString { "it[${it - 1}] as T$it" }

            appendln("""
            @JvmName("and$i") inline infix fun <$reifiedNext>
                AndCombinator<Tuple$i$generics>.and(p${i + 1}: Parser<T${i + 1}>) =
                AndCombinator(consumers + p${i + 1}, {
                    Tuple${i + 1}($casts)
                })
                """.trimIndent() + "\n")
        }
    }

    File(outputFile).apply {
        parentFile.mkdirs()
        writeText(resultCode)
    }
}