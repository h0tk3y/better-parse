import org.gradle.api.Project

val Project.kotlinVersion: String
    get() = project.property("kotlinVersion").toString()

val Project.benchmarkVersion: String
    get() = project.property("benchmarkVersion").toString()

val Project.serializationVersion: String
    get() = project.property("serializationVersion").toString()

private const val serializationJsonPrefix = "org.jetbrains.kotlinx:kotlinx-serialization-json"
private const val benchmarksRuntimePrefix = "org.jetbrains.kotlinx:kotlinx.benchmark.runtime"

data class ModuleSuffixes(
    val common: String,
    val jvm: String?,
    val js: String?,
    val native: String?
)

data class MultiplatformLibraryDependency(
    val rootModule: String,
    val version: String,
    val moduleSuffixes: ModuleSuffixes
) {
    private fun notation(suffix: String?): String? = suffix?.let { "${rootModule}$it:$version" }
    val common: String = notation(moduleSuffixes.common)!!
    val jvm: String? = notation(moduleSuffixes.jvm)
    val js: String? = notation(moduleSuffixes.js)
    val native: String? = notation(moduleSuffixes.native)
}

private val mppNamingScheme = ModuleSuffixes("", null, null, null)

val Project.benchmark
    get() = MultiplatformLibraryDependency(
        benchmarksRuntimePrefix,
        benchmarkVersion,
        mppNamingScheme
    )
val Project.serialization
    get() = MultiplatformLibraryDependency(
        serializationJsonPrefix,
        serializationVersion,
        mppNamingScheme
    )
