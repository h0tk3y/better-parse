@file:Suppress("UnstableApiUsage")

pluginManagement {
    @Suppress("UNCHECKED_CAST")
    val configureRepositories =
        gradle.parent!!.sharedServices.registrations["repositories"].service.get()
            as (RepositoryHandler, isPlugins: Boolean) -> Unit

    val kotlinVersion = System.getProperty("build.kotlinVersion")

    configureRepositories(repositories, true)
    gradle.allprojects { configureRepositories(repositories, false) }

    plugins {
        kotlin("jvm").version(kotlinVersion)
    }
}