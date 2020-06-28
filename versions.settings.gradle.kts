package com.github.h0tk3y.betterParse.build

import org.gradle.api.plugins.ExtraPropertiesExtension
import kotlin.reflect.full.memberProperties

val kotlinVersion = KotlinPlugin.V14M2

enum class KotlinPlugin {
    V1372, V14M2
}

val versions = when (kotlinVersion) {
    KotlinPlugin.V1372 -> Versions(
        version = "0.4.0",
        kotlinVersion = "1.3.72",
        serializationVersion = "0.20.0",
        benchmarkVersion = "0.2.0-dev-8"
    )
    KotlinPlugin.V14M2 -> Versions(
        version = "0.4.0-1.4-M2",
        kotlinVersion = "1.4-M2",
        serializationVersion = "0.20.0-1.4-M2",
        benchmarkVersion = "0.2.0-mpp-dev-5"
    )
}

// ---

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
    fun ExtraPropertiesExtension.addExt() { set(key, value) }
    settings.extensions.extraProperties.addExt()
    gradle.allprojects { extensions.extraProperties.addExt() }
}
