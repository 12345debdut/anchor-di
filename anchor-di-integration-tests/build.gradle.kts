@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(project(":anchor-di-api"))
            implementation(project(":anchor-di-core"))
            implementation(project(":anchor-di-presentation"))
        }
    }
}
