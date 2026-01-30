plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
}

dependencies {
    add("jvmMainImplementation", project(":anchor-di-api"))
    add("jvmMainImplementation", "com.google.devtools.ksp:symbol-processing-api:${libs.versions.ksp.get()}")
    add("jvmTestImplementation", libs.kotlin.test)
}
