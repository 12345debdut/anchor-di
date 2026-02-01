plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    id("signing")
}

kotlin {
    androidLibrary {
        namespace = "com.debdut.anchordi.android"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project(":anchor-di-api"))
            implementation(project(":anchor-di-core"))
            implementation(project(":anchor-di-presentation"))
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
        }
    }
}
