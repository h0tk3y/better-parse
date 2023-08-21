import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeTargetPreset
import java.net.URI

plugins {
    kotlin("multiplatform")

    id("maven-publish")
    id("signing")
}

kotlin {
    explicitApi()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalMultiplatform")
        }

        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        create("nativeMain") {
            dependsOn(commonMain.get())
        }
    }

    jvm {
        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-junit"))
        }
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }

    js(IR) {
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
            compilations.getByName("main") {
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

kotlin.sourceSets.commonMain {
    kotlin.srcDirs(files().builtBy(codegen))
}

//endregion

//region Publication

val publicationsFromWindows = listOf("mingwX64", "mingwX86")

val publicationsFromMacos =
    kotlin.targets.names.filter {
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

publishing {
    repositories {
        maven {
            name = "central"
            val sonatypeUsername = "h0tk3y"
            url = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2")

            credentials {
                username = sonatypeUsername
                password = findProperty("sonatypePassword") as? String
            }
        }
    }
}

// Add a Javadoc JAR to each publication as required by Maven Central:

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.value("javadoc")
    // TODO: instead of a single empty Javadoc JAR, generate real documentation for each module
}

publishing {
    publications.withType<MavenPublication>().all {
        artifact(javadocJar)
    }
}

fun customizeForMavenCentral(pom: org.gradle.api.publish.maven.MavenPom) = pom.withXml {
    fun groovy.util.Node.add(key: String, value: String) {
        appendNode(key).setValue(value)
    }

    fun groovy.util.Node.node(key: String, content: groovy.util.Node.() -> Unit) {
        appendNode(key).also(content)
    }

    asNode().run {
        add("name", "better-parse")
        add(
            "description",
            "A library that provides a set of parser combinator tools for building parsers and translators in Kotlin."
        )
        add("url", "https://github.com/h0tk3y/better-parse")
        node("organization") {
            add("name", "com.github.h0tk3y")
            add("url", "https://github.com/h0tk3y")
        }
        node("issueManagement") {
            add("system", "github")
            add("url", "https://github.com/h0tk3y/better-parse/issues")
        }
        node("licenses") {
            node("license") {
                add("name", "Apache License 2.0")
                add("url", "https://raw.githubusercontent.com/h0tk3y/better-parse/master/LICENSE")
                add("distribution", "repo")
            }
        }
        node("scm") {
            add("url", "https://github.com/h0tk3y/better-parse")
            add("connection", "scm:git:git://github.com/h0tk3y/better-parse")
            add("developerConnection", "scm:git:ssh://github.com/h0tk3y/better-parse.git")
        }
        node("developers") {
            node("developer") {
                add("name", "h0tk3y")
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication>().all {
        customizeForMavenCentral(pom)

        // Signing requires that
        // `signing.keyId`, `signing.password`, and `signing.secretKeyRingFile` are provided as Gradle properties
        signing.sign(this@all)
    }
}

//endregion