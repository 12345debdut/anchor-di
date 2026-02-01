---
sidebar_position: 1
---

# Installation Setup

Add Anchor DI to your Kotlin Multiplatform project.

## Prerequisites

- Kotlin 1.9+
- KSP 2.3+
- Gradle 8+

## Step 1: Add KSP Plugin

```kotlin
// build.gradle.kts (project root)
plugins {
    id("com.google.devtools.ksp") version "2.3.5" apply false
}
```

## Step 2: Add Dependencies to Shared Module

For **KMP + Compose Multiplatform**:

```kotlin
// shared/build.gradle.kts or commonMain source set
plugins {
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.12345debdut:anchor-di-api:0.1.0")
            implementation("io.github.12345debdut:anchor-di-core:0.1.0")
            implementation("io.github.12345debdut:anchor-di-compose:0.1.0")
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosSimulatorArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
}
```

For **KMP without Compose**, omit `anchor-di-compose` and use `anchor-di-api`, `anchor-di-core`, optionally `anchor-di-presentation`, and `anchor-di-android` for ActivityScope.

## Step 3: Initialize at Startup

Call `Anchor.init(*getAnchorContributors())` once at app startup:

- **Android:** In `Application.onCreate()` or before the first composable
- **iOS:** In your app entry point (e.g. `main()` or `@main` App init)
- **Desktop / JVM:** Before creating any UI or resolving dependencies

The generated `getAnchorContributors()` function returns an array of `ComponentBindingContributor` implementations. KSP generates it in `commonMain` metadata; on iOS, you need an actual in both `iosArm64Main` and `iosSimulatorArm64Main` (see [Platform-Specific Setup](/installation/platform-specific)).

## Step 4: Build

Run a build so KSP can process your code and generate the contributor. After that, `getAnchorContributors()` will be available.
