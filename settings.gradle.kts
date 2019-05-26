pluginManagement {
    repositories {
        jcenter()
        maven("https://plugins.gradle.org/m2/")
        maven("https://dl.bintray.com/salomonbrys/gradle-plugins")
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.github.salomonbrys.gradle.kotlin.js.")) {
                useModule("com.github.salomonbrys.gradle.kotlin.js:kotlin-js-gradle-utils:1.2.0")
            }
        }
    }
}

include(":demo:demo-jvm", ":demo:demo-js", ":demo:demo-native")

rootProject.name = "better-parse"

if (System.getProperty("useGradleMetadata") == "true") {
    rootProject.name = "better-parse-multiplatform"
    enableFeaturePreview("GRADLE_METADATA")
}