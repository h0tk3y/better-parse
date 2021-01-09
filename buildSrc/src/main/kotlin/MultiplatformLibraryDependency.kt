import org.gradle.api.Project

val Project.kotlinVersion: String
    get() = project.property("kotlinVersion").toString()

val Project.benchmarkVersion: String
    get() = project.property("benchmarkVersion").toString()

val Project.serializationVersion: String
    get() = project.property("serializationVersion").toString()

private const val serializationJsonPrefix = "org.jetbrains.kotlinx:kotlinx-serialization-json"
private const val benchmarksRuntimePrefix = "org.jetbrains.kotlinx:kotlinx.benchmark.runtime"

data class MultiplatformLibraryDependency(
    val rootModule: String,
    val version: String
) {
    private fun notation(suffix: String?): String? = suffix?.let { "${rootModule}$it:$version" }
    val common: String = notation("")!!
    val jvm: String? = notation(null)
    val js: String? = notation(null)
    val native: String? = notation(null)
}

val Project.benchmark
    get() = MultiplatformLibraryDependency(benchmarksRuntimePrefix, benchmarkVersion)

val Project.serialization
    get() = MultiplatformLibraryDependency(serializationJsonPrefix, serializationVersion)
