@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    id("signing")
}

kotlin {
    jvm()
    wasmJs {
        browser()
    }
    androidLibrary {
        namespace = "com.debdut.anchordi.runtime"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "AnchorDiRuntime"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":anchor-di-api"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
