@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeCompiler)
    id("signing")
}

kotlin {
    androidLibrary {
        namespace = "com.debdut.anchordi.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()
    wasmJs {
        browser()
        binaries.executable()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "AnchorDiCompose"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":anchor-di-api"))
            implementation(project(":anchor-di-core"))
            implementation(project(":anchor-di-presentation"))
            implementation(libs.compose.runtime)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
        }
        androidMain.dependencies {
            implementation(project(":anchor-di-android"))
            implementation(libs.androidx.navigation.compose)
        }
    }
}
