@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()
    wasmJs {
        outputModuleName = "composeApp"
        browser()
        binaries.executable()
    }
    androidLibrary {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "com.debdut.simpletemplate"
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources.enable = true
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "AnchorDI"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.cio)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(project(":anchor-di-api"))
            implementation(project(":anchor-di-runtime"))
            implementation(project(":anchor-di-compose"))
            implementation(project(":anchor-di-navigation"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.jetbrains.navigation3.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":anchor-di-ksp"))
    add("kspAndroid", project(":anchor-di-ksp"))
    add("kspJvm", project(":anchor-di-ksp"))
    add("kspWasmJs", project(":anchor-di-ksp"))
    add("kspIosArm64", project(":anchor-di-ksp"))
    add("kspIosSimulatorArm64", project(":anchor-di-ksp"))
}

compose.desktop {
    application {
        mainClass = "com.debdut.simpletemplate.MainKt"
    }
}

// For multi-module projects: set anchorDiModuleId so each module generates a unique contributor.
// Then combine in getAnchorContributors: arrayOf(AnchorGenerated_composeapp, AnchorGenerated_featureX)
ksp {
    arg("anchorDiModuleId", "composeapp")
}
