plugins {
    kotlin("js")
}

dependencies {
    implementation(rootProject)
}

kotlin.js().nodejs()

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

    from(main.compileKotlinTaskProvider.map { it.destinationDir })
    from(kotlin.sourceSets.main.get().resources) { include("*.html") }
    into("${buildDir}/web")
}

tasks.assemble {
    dependsOn(assembleWeb)
}