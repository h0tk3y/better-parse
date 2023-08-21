plugins {
    kotlin("js")
}

dependencies {
    implementation(rootProject)
}

kotlin.js("IR") {
    browser()
    binaries.executable()
}

var assembleWeb = task<Sync>("assembleWeb") {
    val main by kotlin.js().compilations.getting

    from(project.provider {
        main.compileDependencyFiles.map { it.absolutePath }.map(::zipTree).map {
            it.matching {
                include("*.js")
                exclude("**/META-INFΩ/**")
            }
        }
    })

    from(kotlin.sourceSets.main.get().resources) { include("*.html") }
    into("${buildDir}/web")
}

tasks.assemble {
    dependsOn(assembleWeb)
}