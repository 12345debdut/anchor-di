@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
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
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            // Provides Dispatchers.Main on JVM (required by lifecycle-viewmodel-compose / collectAsStateWithLifecycle)
            implementation(libs.kotlinx.coroutines.swing)
        }
        wasmJsMain.dependencies {
            // Compose Web uses same common deps; add npm deps here if needed (e.g. timezone)
        }
        commonMain.dependencies {
            implementation(project(":anchor-di-api"))
            implementation(project(":anchor-di-runtime"))
            implementation(project(":anchor-di-compose"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
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
