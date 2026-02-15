---
sidebar_position: 2
---

# Gradle Plugin

The **Anchor DI Gradle plugin** automates KSP setup, dependency management, and configuration for Kotlin Multiplatform projects. Use it to reduce boilerplate and avoid common setup mistakes.

---

## Quick Setup

Add the plugin to your shared module (the one with `commonMain`):

```kotlin
// build.gradle.kts (shared module)
plugins {
    kotlin("multiplatform")
    id("io.github.12345debdut.anchordi") version "x.x.x"
}

anchorDi {
    moduleId.set("myapp")      // For multi-module; optional
    includeCompose.set(true)   // Add anchor-di-compose; default true
}
```

The plugin automatically:

- Applies the KSP plugin
- Adds `anchor-di-api` and `anchor-di-core` to `commonMain`
- Adds `anchor-di-compose` when `includeCompose` is true
- Adds the KSP processor (`anchor-di-ksp`) for each configured target (Android, JVM, iOS, Wasm)
- Sets `anchorDiModuleId` when `moduleId` is configured (for multi-module)

---

## Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `moduleId` | `String` | — | For multi-module projects. Generates `AnchorGenerated_<moduleId>`. |
| `includeCompose` | `Boolean` | `true` | Add `anchor-di-compose` (anchorInject, viewModelAnchor, etc.). |
| `version` | `String` | Plugin version | Override the Anchor DI library version. |

---

## KMP Without Compose

If you use native UI or shared logic only, disable Compose:

```kotlin
anchorDi {
    includeCompose.set(false)
}
```

You can still add `anchor-di-presentation` or `anchor-di-android` manually if needed.

---

## Multi-Module

In each module that contributes DI bindings, set a unique `moduleId`:

```kotlin
// feature-auth/build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("io.github.12345debdut.anchordi") version "x.x.x"
}

anchorDi {
    moduleId.set("auth")
}
```

Then aggregate contributors in your app module. See [Multi-Module Setup](multi-module).

---

## Version Catalog

If you use a [Gradle version catalog](https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog-plugin), add:

```toml
# gradle/libs.versions.toml
[plugins]
anchorDi = { id = "io.github.12345debdut.anchordi", version = "x.x.x" }
```

Then in your module:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.anchorDi)
}
```

---

## Local Development (anchor-di repo)

When developing Anchor DI itself, the plugin detects local projects and uses `project(":anchor-di-api")` instead of published artifacts. Publish the plugin to Maven Local first:

```bash
./gradlew :anchor-di-gradle-plugin:publishToMavenLocal
```

Ensure `mavenLocal()` is in `pluginManagement` repositories (see [settings.gradle.kts](https://github.com/12345debdut/anchor-di/blob/main/settings.gradle.kts)).

---

## Manual Setup

If you prefer manual configuration or need more control, see [Installation Setup](setup) for the step-by-step guide without the plugin.
