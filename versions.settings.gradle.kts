package com.github.h0tk3y.betterParse.build

import org.gradle.api.plugins.ExtraPropertiesExtension
import kotlin.reflect.full.memberProperties

val kotlinVersion = KotlinPlugin.V1900

enum class KotlinPlugin {
    V1900
}

val versions = when (kotlinVersion) {
    KotlinPlugin.V1900 -> Versions(
        version = "0.4.4",
        kotlinVersion = "1.9.0",
        serializationVersion = "1.5.1",
        benchmarkVersion = "0.4.9"
    )
}

// Register all versions as system properties:

versions.javaClass.kotlin.memberProperties.forEach { property ->
    val value = property.get(versions)
    addGlobalProperty(property.name, value.toString())
}

gradle.allprojects { version = versions.version }

data class Versions(
    val version: String,
    val kotlinVersion: String,
    val serializationVersion: String,
    val benchmarkVersion: String
)

fun addGlobalProperty(key: String, value: String) {
    System.setProperty("build.$key", value)
    fun ExtraPropertiesExtension.addExt() { set(key, value) }
    settings.extensions.extraProperties.addExt()
    gradle.allprojects { extensions.extraProperties.addExt() }
}
