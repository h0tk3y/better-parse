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
                implementation(benchmark)
                implementation(serialization)
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