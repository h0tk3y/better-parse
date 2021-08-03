plugins {
    kotlin("multiplatform")
    kotlin("plugin.allopen")
    id("org.jetbrains.kotlinx.benchmark")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(rootProject)
                implementation(benchmark)
                implementation(serialization)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    jvm {
        compilations["main"].apply {
            kotlinOptions.jvmTarget = "1.8"
        }

        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-junit"))
        }
    }

    js {
        nodejs()

        compilations["main"].defaultSourceSet.dependencies {
            implementation(kotlin("stdlib-js"))
        }
        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-js"))
        }
    }

    macosX64 { }
    linuxX64 { }
    mingwX64 { }
}

allOpen.annotation("org.openjdk.jmh.annotations.State")

benchmark {
    targets.register("jvm")
    targets.register("js")
    targets.register("macosX64")
    targets.register("linuxX64")
    targets.register("mingwX64")

    configurations["main"].apply {
        warmups = 5
        iterations = 10
    }
}