@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeCompiler)
    id("signing")
}

kotlin {
    jvm()
    wasmJs {
        browser()
    }
    androidLibrary {
        namespace = "com.debdut.anchordi.navigation.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "AnchorDiNavigationCompose"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":anchor-di-navigation"))
            implementation(project(":anchor-di-core"))
            implementation(libs.compose.runtime)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
        }
        androidMain.dependencies {
            implementation(libs.androidx.navigation.compose)
        }
    }
}
