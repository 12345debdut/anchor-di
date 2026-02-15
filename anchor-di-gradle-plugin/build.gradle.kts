plugins {
    id("org.jetbrains.kotlin.jvm") // No version - Kotlin already on classpath from root
    `java-gradle-plugin`
    `maven-publish`
}

// Group must match plugin id prefix so Gradle resolves from Maven Central (verified namespace).
group = "io.github.12345debdut"
version = project.findProperty("LIBRARY_VERSION")?.toString()
    ?: project.findProperty("VERSION")?.toString() ?: "0.1.0"

gradlePlugin {
    plugins {
        create("anchorDi") {
            id = "io.github.12345debdut.anchordi"
            displayName = "Anchor DI"
            description = "Gradle plugin for Anchor DI - compile-time dependency injection for Kotlin Multiplatform"
            implementationClass = "com.debdut.anchordi.gradle.AnchorDiPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
}

// Publish to Maven Local and Sonatype/Maven Central (see gradle/publish-convention-gradle-plugin.gradle.kts)
apply(from = rootProject.file("gradle/publish-convention-gradle-plugin.gradle.kts"))
