plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kover)
    id("signing")
}

kotlin {
    jvm()
}

dependencies {
    add("jvmMainImplementation", project(":anchor-di-api"))
    add("jvmMainImplementation", "com.google.devtools.ksp:symbol-processing-api:${libs.versions.ksp.get()}")
    add("jvmTestImplementation", libs.kotlin.test)
}
