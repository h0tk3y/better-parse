import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
}

kotlin {
    macosX64("macos")
    linuxX64("linux")
    mingwX64("windows")

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(rootProject)
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        kotlin.targets.withType<KotlinNativeTarget>().all {
            compilations.getByName("main") {
                defaultSourceSet.dependsOn(nativeMain)
                binaries.executable { }
            }
        }
    }
}
