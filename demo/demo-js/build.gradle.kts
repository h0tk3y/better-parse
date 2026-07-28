plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        nodejs()
    }

    sourceSets {
        jsMain {
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")

            dependencies {
                implementation(rootProject)
            }
        }
    }
}

val assembleWeb = tasks.register<Sync>("assembleWeb") {
    val main = kotlin.js().compilations.getByName("main")

    from(project.provider {
        main.compileDependencyFiles.filter { it.isFile }.map(::zipTree).map {
            it.matching {
                include("*.js")
                exclude("**/META-INFΩ/**")
            }
        }
    })

    from(main.compileTaskProvider.map { it.destinationDirectory })
    from(kotlin.sourceSets.getByName("jsMain").resources) { include("*.html") }
    into(layout.buildDirectory.dir("web"))
}

tasks.assemble {
    dependsOn(assembleWeb)
}
