package com.debdut.anchordi.gradle

import org.gradle.api.provider.Property

/**
 * Extension for configuring the Anchor DI Gradle plugin.
 *
 * @param project The project this extension is attached to
 */
abstract class AnchorDiExtension(
    private val project: org.gradle.api.Project,
) {
    /**
     * Module ID for multi-module projects. When set, KSP will generate
     * `AnchorGenerated_<moduleId>` instead of `AnchorGenerated`.
     *
     * Example: `moduleId.set("composeapp")` for the compose app module.
     */
    abstract val moduleId: Property<String>

    /**
     * Whether to add `anchor-di-compose` (anchorInject, viewModelAnchor, etc.).
     * Default: true for Compose Multiplatform projects.
     */
    abstract val includeCompose: Property<Boolean>

    /**
     * Override the Anchor DI library version. Defaults to the plugin version.
     * Only used when not using local projects (e.g. in the anchor-di repo).
     */
    abstract val version: Property<String>

    init {
        includeCompose.convention(true)
        version.convention(
            project.provider {
                val v = project.version.toString()
                when {
                    v != "unspecified" -> v
                    else -> {
                        val rootV = project.rootProject.version.toString()
                        if (rootV != "unspecified") {
                            rootV
                        } else {
                            project.rootProject.findProperty("LIBRARY_VERSION")?.toString()
                                ?: project.findProperty("LIBRARY_VERSION")?.toString()
                                ?: "0.1.0"
                        }
                    }
                }
            },
        )
    }
}
