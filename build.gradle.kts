plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kover)
}

// Aggregated test coverage for library modules (JVM tests only)
dependencies {
    kover(project(":anchor-di-core"))
    kover(project(":anchor-di-ksp"))
}

val publishableModules = setOf(
    "anchor-di-api",
    "anchor-di-core",
    "anchor-di-ksp",
    "anchor-di-android",
    "anchor-di-navigation",
    "anchor-di-navigation-compose",
    "anchor-di-compose"
)

subprojects {
    if (name in publishableModules) {
        group = project.findProperty("LIBRARY_GROUP")?.toString() ?: "io.github.12345debdut"
        version = project.findProperty("LIBRARY_VERSION")?.toString()
            ?: project.findProperty("VERSION")?.toString() ?: "0.1.0"
        apply(plugin = "publish-convention")
    }
}