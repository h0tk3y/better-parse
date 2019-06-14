@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")
// fixme remove this suppression

import org.jetbrains.gradle.benchmarks.benchmark

plugins {
    kotlin("multiplatform")
    kotlin("plugin.allopen").version("1.3.50-dev-759")

    id("kotlinx.team.node")
    id("org.jetbrains.gradle.benchmarks.plugin").version("0.1.7-dev-24")
}

repositories {
    maven("https://dl.bintray.com/orangy/maven")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(rootProject)
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.gradle.benchmarks:runtime:0.1.7-dev-24")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.11.0")
            }
        }
    }

    jvm {
        compilations["main"].apply {
            kotlinOptions.jvmTarget = "1.8"

            defaultSourceSet.dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.11.0")
            }
        }
    }

    js {
        compilations["main"].apply {
            defaultSourceSet.dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("org.jetbrains.gradle.benchmarks:runtime-js:0.1.7-dev-24")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.11.0")
            }
        }
    }
}

allOpen.annotation("org.openjdk.jmh.annotations.State")

node {
    version = "10.15.1"
}

benchmark {
    targets.register("jvm")
    targets.register("js")
}