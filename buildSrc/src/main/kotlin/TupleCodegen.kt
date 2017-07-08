import java.io.File

fun tupleCodegen(maxN: Int, outputFile: String) {
    fun genericsStr(i: Int) = (1..i).joinToString(prefix = "<", postfix = ">") { "T$it" }

    val resultCode = buildString {
        appendln("package com.github.h0tk3y.betterParse.utils\n")

        for (i in 1..maxN) {
            val generics = genericsStr(i)
            val ctorParameters = (1..i).joinToString { "val t$it: T$it" }
            val components = (1..i).joinToString { "t$it" }
            val genericsBoundByT = (1..i).joinToString { "T$it : T" }

            appendln("""
                data class Tuple$i$generics($ctorParameters) : Tuple
                val <T, $genericsBoundByT> Tuple$i$generics.components get() = listOf($components)
            """.trimIndent())

            appendln()
        }
    }

    File(outputFile).apply {
        parentFile.mkdirs()
        writeText(resultCode)
    }
}