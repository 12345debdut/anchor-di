plugins {
    `kotlin-dsl`
}

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

// No plugin dependencies needed — the convention plugin uses Gradle's generic
// extension API and reflection to stay classpath-neutral, avoiding "plugin already
// on classpath" conflicts with the root build.
