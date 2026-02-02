plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kover)
    id("signing")
}

kotlin {
    jvm()
}

// Only publish the JVM variant (anchor-di-ksp-jvm). The root kotlinMultiplatform publication
// can end up without a .pom in some setups, which fails Maven Central validation.
afterEvaluate {
    publishing.publications.withType<org.gradle.api.publish.maven.MavenPublication>()
        .matching { it.name == "kotlinMultiplatform" }
        .configureEach {
            val taskName = "publish${name.replaceFirstChar(Char::uppercaseChar)}PublicationToSonatypeRepository"
            tasks.matching { it.name == taskName }.configureEach { enabled = false }
        }
}

dependencies {
    add("jvmMainImplementation", project(":anchor-di-api"))
    add("jvmMainImplementation", "com.google.devtools.ksp:symbol-processing-api:${libs.versions.ksp.get()}")
    add("jvmTestImplementation", libs.kotlin.test)
}
