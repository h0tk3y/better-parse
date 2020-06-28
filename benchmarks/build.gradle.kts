plugins {
    kotlin("multiplatform")
    kotlin("plugin.allopen")
    id("kotlinx.benchmark")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(rootProject)
                implementation(kotlin("stdlib-common"))
                implementation(benchmark.common)
                implementation(serialization.common)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }

    jvm {
        compilations["main"].apply {
            kotlinOptions.jvmTarget = "1.8"

            defaultSourceSet.dependencies {
                implementation(kotlin("stdlib"))
                benchmark.jvm?.let(::implementation)
                serialization.jvm?.let(::implementation)
            }
        }

        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-junit"))
        }
    }

    js {
        nodejs()

        compilations["main"].defaultSourceSet.dependencies {
            implementation(kotlin("stdlib-js"))
            benchmark.js?.let(::implementation)
            serialization.js?.let(::implementation)
        }
        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-js"))
        }
    }
}

allOpen.annotation("org.openjdk.jmh.annotations.State")

benchmark {
    targets.register("jvm")
    targets.register("js")

    configurations["main"].apply {
        warmups = 5
        iterations = 10
    }
}