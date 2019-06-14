pluginManagement {
    repositories {
        jcenter()
        maven("https://plugins.gradle.org/m2/")
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
        maven("https://dl.bintray.com/salomonbrys/gradle-plugins")
        maven("https://dl.bintray.com/orangy/maven")
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.github.salomonbrys.gradle.kotlin.js.")) {
                useModule("com.github.salomonbrys.gradle.kotlin.js:kotlin-js-gradle-utils:1.2.0")
            }
            if (requested.id.id.startsWith("kotlinx.team.")) {
                useModule("kotlinx.team:kotlinx.team.infra:0.1.0-dev-47")
            }
        }
    }
}

 include(":benchmarks", ":demo:demo-jvm", ":demo:demo-js", ":demo:demo-native")

rootProject.name = "better-parse-multiplatform"

if (System.getProperty("useGradleMetadata") != "false") {
    enableFeaturePreview("GRADLE_METADATA")
} else {
    rootProject.name = "better-parse"
}