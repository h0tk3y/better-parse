@file:Suppress("UnstableApiUsage")

import org.gradle.api.artifacts.dsl.RepositoryHandler

pluginManagement {
    abstract class RepositorySetup :
        BuildServiceParameters, (RepositoryHandler, Boolean) -> Unit, BuildService<RepositorySetup> {
        override fun invoke(repositories: RepositoryHandler, isPlugins: Boolean): Unit = with(repositories) {
            jcenter()
            maven("https://dl.bintray.com/kotlin/kotlinx")
            if (isPlugins) gradlePluginPortal()
        }
    }

    val configureRepositories = gradle.sharedServices.registerIfAbsent("repositories", RepositorySetup::class) { }.get()
    configureRepositories(repositories, true)
    gradle.allprojects { configureRepositories(repositories, false) }
    apply(from = "versions.settings.gradle.kts")
    val kotlinVersion: String by settings
    val benchmarkVersion: String by settings

    plugins {
        kotlin("multiplatform") version kotlinVersion
        kotlin("js") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
        id("kotlinx.benchmark") version benchmarkVersion
    }
}

rootProject.name = "better-parse"
include(":benchmarks", ":demo:demo-jvm", ":demo:demo-js", ":demo:demo-native")
