
import org.apache.tools.ant.taskdefs.condition.Os
import java.net.URI

plugins {
    kotlin("multiplatform").version("1.3.31")
    kotlin("plugin.allopen").version("1.3.31")

    id("com.github.salomonbrys.gradle.kotlin.js.mpp-tests.node").version("1.2.0")
    id("org.jetbrains.gradle.benchmarks.plugin").version("0.1.7-dev-22")
    `maven-publish`
}

group = "com.github.h0tk3y.betterParse"
version = "0.4.0"

val kotlinVersion = "1.3.31"

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common", kotlinVersion))
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test-common", kotlinVersion))
                implementation(kotlin("test-annotations-common", kotlinVersion))
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain.get())
        }
    }

    jvm {
        compilations.named("main") {
            kotlinOptions.jvmTarget = "1.6"

            defaultSourceSet.dependencies {
                implementation(kotlin("stdlib", kotlinVersion))
            }
        }
        compilations.named("test") {
            defaultSourceSet.dependencies {
                implementation(kotlin("test-junit", kotlinVersion))
            }
        }
    }

    js {
        compilations["main"].defaultSourceSet.dependencies {
            implementation(kotlin("stdlib-js", kotlinVersion))
        }
        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-js", kotlinVersion))
        }

        compilations.all {
            kotlinOptions.moduleKind = "umd"
        }

        kotlinJsNodeTests {
            thisTarget()
        }
    }

    presets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetPreset::class).forEach {
        targetFromPreset(it) {
            compilations.getByName("main") {
                defaultSourceSet.dependsOn(sourceSets["nativeMain"])
            }
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.useExperimentalAnnotation("kotlin.ExperimentalMultiplatform")
}

// Code generation

val codegen by tasks.registering {
    val maxTupleSize = 16

    andCodegen(
        maxTupleSize,
        kotlin.sourceSets.commonMain.get().kotlin.srcDirs.first().absolutePath + "/generated/andFunctions.kt"
    )
    tupleCodegen(
        maxTupleSize,
        kotlin.sourceSets.commonMain.get().kotlin.srcDirs.first().absolutePath + "/generated/tuples.kt"
    )
}

kotlin.targets.all {
    compilations.all {
        tasks[compileKotlinTaskName].dependsOn(codegen)
    }
}

// Publication

val publicationsFromLinux =
    kotlin.targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class).names.filter {
        !it.startsWith("mingw") && !it.startsWith("macos") && !it.startsWith("ios")
    } + listOf("kotlinMultiplatform", "metadata", "js", "jvm")

val publicationsFromWindows = listOf("mingwX64")

val publicationsFromMacos =
    kotlin.targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class).names.filter {
        it.startsWith("macos") || it.startsWith("ios")
    }

val publicationsFromThisPlatform = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> publicationsFromWindows
    Os.isFamily(Os.FAMILY_MAC) -> publicationsFromMacos
    Os.isFamily(Os.FAMILY_UNIX) -> publicationsFromLinux
    else -> error("Expected Windows, Mac, or Linux host")
}

tasks.withType(AbstractPublishToMaven::class).all {
    onlyIf { publication.name in publicationsFromThisPlatform }
}

publishing {
    repositories {
        maven {
            name = "bintray"
            val bintrayUsername = "hotkeytlt"
            val bintrayRepoName = "maven"
            val bintrayPackageName = "better-parse"
            url = URI(
                "https://api.bintray.com/maven/$bintrayUsername/$bintrayRepoName/$bintrayPackageName/;publish=0"
            )

            credentials {
                username = findProperty("bintray_user") as? String
                password = findProperty("bintray_api_key") as? String
            }
        }
    }
}

// Benchmarking

node {
    version = "10.15.1"
}

repositories {
    jcenter()
    maven("https://dl.bintray.com/orangy/maven")
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

kotlin.jvm().compilations.create("benchmark") {
    defaultSourceSet.dependencies {
        implementation("org.jetbrains.gradle.benchmarks:runtime-jvm:0.1.7-dev-22")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.9.0")
        implementation(kotlin.jvm().compilations["main"].run { compileDependencyFiles + output.allOutputs })
    }
}

benchmark {
    defaults {
        iterations = 5 // number of iterations
        iterationTime = 300 // time in ms per iteration
    }

    configurations {
        create("jvmBenchmark")
    }
}