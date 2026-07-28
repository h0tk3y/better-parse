pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    apply(from = "versions.settings.gradle.kts")
    val kotlinVersion: String by settings
    val benchmarkVersion: String by settings

    plugins {
        kotlin("multiplatform").version(kotlinVersion)
        kotlin("jvm").version(kotlinVersion)
        kotlin("plugin.allopen").version(kotlinVersion)
        id("org.jetbrains.kotlinx.benchmark").version(benchmarkVersion)
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "better-parse"

include(":benchmarks", ":demo:demo-jvm", ":demo:demo-js", ":demo:demo-native")
