package com.github.h0tk3y.betterParse.build

import org.gradle.api.plugins.ExtraPropertiesExtension
import kotlin.reflect.full.memberProperties

val kotlinVersion = KotlinPlugin.V1431

enum class KotlinPlugin {
    V1431, /* TODO: support 1.5 pre-releases */
}

val versions = when (kotlinVersion) {
    KotlinPlugin.V1431 -> Versions(
        version = "0.4.2",
        kotlinVersion = "1.4.31",
        serializationVersion = "1.1.0",
        benchmarkVersion = "0.3.0"
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
