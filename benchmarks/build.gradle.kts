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
        val main by compilations.getting {
            kotlinOptions.jvmTarget = "1.8"

            defaultSourceSet.dependencies {
                benchmark.jvm?.let(::implementation)
                serialization.jvm?.let(::implementation)
            }
        }

        val test by compilations.getting {
            defaultSourceSet.dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }

    js {
        nodejs()

        compilations["main"].defaultSourceSet.dependencies {
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

    val main by configurations.getting {
        warmups = 5
        iterations = 10
    }
}
