import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeTargetPreset
import java.net.URI

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    explicitApiWarning()

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalMultiplatform")
        }

        commonTest.get().dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }

        val nativeMain by creating {
            dependsOn(commonMain.get())
        }
    }

    jvm {
        val test by compilations.getting {
            defaultSourceSet.dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        compilations.all {
            kotlinOptions.jvmTarget = "1.6"
        }
    }

    js {
        browser()
        nodejs()

        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-js"))
        }

        compilations.all {
            kotlinOptions.moduleKind = "umd"
        }
    }

    presets.withType<AbstractKotlinNativeTargetPreset<*>>().forEach {
        targetFromPreset(it) {
            val main by compilations.getting {
                defaultSourceSet.dependsOn(sourceSets["nativeMain"])
            }
        }
    }
}

//region Code generation

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

kotlin.sourceSets.commonMain.get().kotlin.srcDirs(files().builtBy(codegen))

//endregion

//region Publication

val publicationsFromWindows = listOf("mingwX64", "mingwX86")

val publicationsFromMacos = kotlin.targets.names.filter {
    it.startsWith("macos") || it.startsWith("ios") || it.startsWith("watchos") || it.startsWith("tvos")
}

val publicationsFromLinux = publishing.publications.names - publicationsFromWindows - publicationsFromMacos

val publicationsFromThisPlatform = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> publicationsFromWindows
    Os.isFamily(Os.FAMILY_MAC) -> publicationsFromMacos
    Os.isFamily(Os.FAMILY_UNIX) -> publicationsFromLinux
    else -> error("Expected Windows, Mac, or Linux host")
}

tasks.withType(AbstractPublishToMaven::class).all {
    onlyIf { publication.name in publicationsFromThisPlatform }
}

publishing.repositories.maven {
    name = "bintray"
    val bintrayUsername = "hotkeytlt"
    val bintrayRepoName = "maven"
    val bintrayPackageName = "better-parse"
    url = URI("https://api.bintray.com/maven/$bintrayUsername/$bintrayRepoName/$bintrayPackageName/;publish=0")

    credentials {
        username = bintrayUsername
        password = findProperty("bintray_api_key") as? String
    }
}

//endregion