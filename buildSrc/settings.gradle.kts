@file:Suppress("UnstableApiUsage")

pluginManagement {
    @Suppress("UNCHECKED_CAST")
    val configureRepositories =
        gradle.parent!!.sharedServices.registrations["repositories"].service.get()
            as (RepositoryHandler, isPlugins: Boolean) -> Unit

    configureRepositories(repositories, true)
    gradle.allprojects {
        configureRepositories(repositories, false)
    }

    val kotlinVersion = System.getProperty("build.kotlinVersion")

    plugins {
        kotlin("jvm") version(kotlinVersion)
    }
}