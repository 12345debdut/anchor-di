/**
 * Convention plugin that configures a Kotlin Multiplatform module for Anchor DI.
 *
 * Automates the boilerplate that consumers otherwise write manually:
 *   1. Applies the KSP plugin (if not already applied)
 *   2. Adds `anchor-di-api` and `anchor-di-core` to commonMain
 *   3. Adds `anchor-di-ksp` processor to all active KMP targets
 *   4. Optionally adds `anchor-di-compose` and `anchor-di-presentation`
 *   5. Sets the `anchorDiModuleId` KSP argument (defaults to project name)
 *
 * ## Usage
 *
 * ```kotlin
 * // build.gradle.kts
 * plugins {
 *     id("anchor-di-convention")
 * }
 *
 * // Optional configuration (defaults shown):
 * anchorDi {
 *     compose = false         // set true to add anchor-di-compose + anchor-di-presentation
 *     moduleId = project.name // override the generated contributor name
 * }
 * ```
 *
 * This replaces the manual 12-line KSP dependency block and anchor-di dependency declarations.
 */

open class AnchorDiExtension {
    /** If true, adds anchor-di-compose and anchor-di-presentation dependencies. */
    var compose: Boolean = false

    /**
     * Module ID used to generate a unique [ComponentBindingContributor] name.
     * For multi-module projects, each module must have a distinct value.
     * Defaults to the Gradle project name (sanitized).
     */
    var moduleId: String? = null
}

val anchorDi = extensions.create<AnchorDiExtension>("anchorDi")

// After evaluation: inspect which KMP targets are active, then wire up KSP + dependencies.
// We expect the consumer to have already applied both the Kotlin Multiplatform and KSP plugins.
afterEvaluate {
    // Verify prerequisites
    require(extensions.findByName("kotlin") != null) {
        "anchor-di-convention requires the Kotlin Multiplatform plugin to be applied first."
    }
    require(pluginManager.hasPlugin("com.google.devtools.ksp")) {
        "anchor-di-convention requires the KSP plugin to be applied first."
    }

    // --- 1. Add runtime dependencies to commonMain ---
    dependencies.add("commonMainImplementation", project(":anchor-di-api"))
    dependencies.add("commonMainImplementation", project(":anchor-di-core"))
    if (anchorDi.compose) {
        dependencies.add("commonMainImplementation", project(":anchor-di-compose"))
    }

    // --- 2. Add KSP processor for each active target ---
    val kspProject = project(":anchor-di-ksp")
    dependencies.add("kspCommonMainMetadata", kspProject)

    // Discover active targets by checking which ksp<Target> configurations exist.
    val kspConfigPattern = Regex("^ksp[A-Z].*")
    configurations.names
        .filter { it.matches(kspConfigPattern) && it != "kspCommonMainMetadata" }
        .forEach { kspConfigName ->
            dependencies.add(kspConfigName, kspProject)
        }

    // --- 3. Set the KSP argument for multi-module contributor naming ---
    val resolvedModuleId =
        anchorDi.moduleId
            ?: project.name.replace(Regex("[^a-zA-Z0-9]"), "").lowercase()

    // Access KSP extension via Gradle's generic API to avoid buildSrc classpath conflicts.
    val kspExtension = extensions.findByName("ksp")
    if (kspExtension != null) {
        // KspExtension.arg(provider, provider) — use reflection to stay classpath-neutral.
        val argMethod =
            kspExtension.javaClass.getMethod(
                "arg",
                String::class.java,
                String::class.java,
            )
        argMethod.invoke(kspExtension, "anchorDiModuleId", resolvedModuleId)
    }
}
