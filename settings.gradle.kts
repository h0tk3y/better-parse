@file:Suppress("UnstableApiUsage")

import org.gradle.api.artifacts.dsl.RepositoryHandler

pluginManagement {
    abstract class RepositorySetup :
        BuildServiceParameters,
        BuildService<RepositorySetup>, (RepositoryHandler, Boolean) -> Unit by { repositoryHandler, isPlugins ->
        with(repositoryHandler) {
            jcenter()
            maven("https://dl.bintray.com/kotlin/kotlinx")
            maven("https://dl.bintray.com/kotlin/kotlin-dev")
            if (isPlugins) {
                gradlePluginPortal()
            }
        }
    }

    val configureRepositories = gradle.sharedServices.registerIfAbsent("repositories", RepositorySetup::class) { }.get()

    configureRepositories(repositories, true)
    gradle.allprojects { configureRepositories(repositories, false) }

    apply(from = "versions.settings.gradle.kts")
    val kotlinVersion: String by settings
    val benchmarkVersion: String by settings
    System.setProperty("build.kotlinVersion", kotlinVersion)

    plugins {
        kotlin("multiplatform").version(kotlinVersion)
        kotlin("js").version(kotlinVersion)
        kotlin("plugin.allopen").version(kotlinVersion)
        id("kotlinx.benchmark").version(benchmarkVersion)
    }
}

rootProject.name = "better-parse"

include(":benchmarks", ":demo:demo-jvm", ":demo:demo-js", ":demo:demo-native")
