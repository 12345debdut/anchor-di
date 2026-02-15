plugins {
    id("org.jetbrains.kotlin.jvm") // No version - Kotlin already on classpath from root
    `java-gradle-plugin`
    `maven-publish`
}

// Group must be 'com.debdut.anchordi' so Gradle can resolve plugin id("com.debdut.anchordi")
// as com.debdut.anchordi:com.debdut.anchordi.gradle.plugin
group = "com.debdut.anchordi"
version = project.findProperty("LIBRARY_VERSION")?.toString()
    ?: project.findProperty("VERSION")?.toString() ?: "0.1.0"

gradlePlugin {
    plugins {
        create("anchorDi") {
            id = "com.debdut.anchordi"
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
