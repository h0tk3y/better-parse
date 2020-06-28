import org.gradle.api.Project

val Project.kotlinVersion: String
    get() = project.property("kotlinVersion").toString()

val Project.benchmarkVersion: String
    get() = project.property("benchmarkVersion").toString()

val Project.serializationVersion: String
    get() = project.property("serializationVersion").toString()

private const val serializationRuntimePrefix = "org.jetbrains.kotlinx:kotlinx-serialization-runtime"
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

val Project.benchmarksNamingScheme get() = when (kotlinVersion) {
    "1.3.72" -> ModuleSuffixes("-metadata", "-jvm", "-js", null)
    else -> mppNamingScheme
}

val Project.serializationNamingScheme get() = when(kotlinVersion) {
    "1.3.72" -> ModuleSuffixes("-common", "", "-js", "-native")
    else -> mppNamingScheme
}

val Project.benchmark get() = MultiplatformLibraryDependency(benchmarksRuntimePrefix, benchmarkVersion, benchmarksNamingScheme)
val Project.serialization get() = MultiplatformLibraryDependency(serializationRuntimePrefix, serializationVersion, serializationNamingScheme)
