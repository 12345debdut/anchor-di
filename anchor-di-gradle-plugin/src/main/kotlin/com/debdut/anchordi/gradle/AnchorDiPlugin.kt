package com.debdut.anchordi.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Gradle plugin for Anchor DI — automates KSP setup, dependencies, and KSP arguments
 * for Kotlin Multiplatform projects.
 *
 * Usage:
 * ```
 * plugins {
 *     kotlin("multiplatform")
 *     id("io.github.12345debdut.anchordi") version "x.x.x"
 * }
 *
 * anchorDi {
 *     moduleId.set("myapp")           // For multi-module; optional
 *     includeCompose.set(true)        // Add anchor-di-compose; default true
 *     version.set("0.1.0")           // Override library version; optional
 * }
 * ```
 */
class AnchorDiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("anchorDi", AnchorDiExtension::class.java, project)

        // Apply KSP plugin if not already applied
        if (!project.plugins.hasPlugin("com.google.devtools.ksp")) {
            project.plugins.apply("com.google.devtools.ksp")
        }

        project.afterEvaluate {
            val libVersion = extension.version.getOrElse(project.version.toString())
            val useLocalProjects = project.rootProject.findProject(":anchor-di-api") != null

            val apiDep: Any =
                if (useLocalProjects) {
                    project.dependencies.project(mapOf("path" to ":anchor-di-api"))
                } else {
                    "io.github.12345debdut:anchor-di-api:$libVersion"
                }
            val coreDep: Any =
                if (useLocalProjects) {
                    project.dependencies.project(mapOf("path" to ":anchor-di-core"))
                } else {
                    "io.github.12345debdut:anchor-di-core:$libVersion"
                }
            val kspDep: Any =
                if (useLocalProjects) {
                    project.dependencies.project(mapOf("path" to ":anchor-di-ksp"))
                } else {
                    "io.github.12345debdut:anchor-di-ksp:$libVersion"
                }
            val composeDep: Any? =
                if (extension.includeCompose.get()) {
                    if (useLocalProjects) {
                        project.dependencies.project(mapOf("path" to ":anchor-di-compose"))
                    } else {
                        "io.github.12345debdut:anchor-di-compose:$libVersion"
                    }
                } else {
                    null
                }

            // Add commonMain dependencies via Kotlin Multiplatform extension
            val kotlinExt = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
            if (kotlinExt != null) {
                val commonMain = kotlinExt.sourceSets.findByName("commonMain")
                if (commonMain != null) {
                    commonMain.dependencies {
                        implementation(apiDep)
                        implementation(coreDep)
                        if (composeDep != null) implementation(composeDep)
                    }
                }
            }

            // Add KSP dependencies for each target
            val kspConfigNames =
                listOf(
                    "kspCommonMainMetadata",
                    "kspAndroid",
                    "kspJvm",
                    "kspIosArm64",
                    "kspIosSimulatorArm64",
                    "kspIosX64",
                    "kspWasmJs",
                )
            val depHandler: DependencyHandler = project.dependencies
            kspConfigNames.forEach { configName ->
                val config = project.configurations.findByName(configName)
                if (config != null) {
                    depHandler.add(configName, kspDep)
                }
            }

            // Configure KSP arguments (moduleId for multi-module)
            extension.moduleId.orNull?.takeIf { it.isNotBlank() }?.let { moduleId ->
                project.extensions.findByName("ksp")?.let { kspExt ->
                    kspExt.javaClass.getMethod("arg", String::class.java, String::class.java)
                        .invoke(kspExt, "anchorDiModuleId", moduleId)
                }
            }

            // Ensure Kotlin compile tasks run after KSP so generated code (e.g. AnchorGenerated_*) is visible
            project.tasks.configureEach { task ->
                val name = task.name
                if (name.startsWith("compile") && name.contains("Kotlin") && !name.contains("Metadata")) {
                    val kspName = name.replace("compile", "ksp")
                    val kspTask = project.tasks.findByName(kspName)
                    if (kspTask != null) {
                        task.dependsOn(kspTask)
                    }
                }
            }
        }
    }
}
