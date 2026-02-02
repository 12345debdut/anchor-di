---
sidebar_position: 1
---

# Kotlin Multiplatform Overview

Anchor DI is **multiplatform by design**. It runs on Android, iOS, Desktop (JVM), and Web (Wasm). Generated code lives in `commonMain`; platform-specific dependencies use `expect` / `actual`. This page explains how Anchor DI fits into a KMP project and what you need to know about source sets and KSP targets.

---

## What is Kotlin Multiplatform?

**Kotlin Multiplatform (KMP)** lets you share code across platforms — Android, iOS, Desktop, Web — while keeping platform-specific code where it belongs. You write business logic (networking, data layer, use cases) once in Kotlin and run it everywhere. Anchor DI is built for this: the same annotations, the same API, the same mental model on all platforms.

---

## Source Sets

In KMP, source sets define where code lives and which platforms it targets. Anchor DI uses the same structure:

| Source Set | Use |
|------------|-----|
| **commonMain** | Shared DI graph, modules, `@Inject` classes. Your main DI setup lives here. |
| **androidMain** | Android-specific modules (e.g. `ActivityScope`), platform providers. |
| **iosArm64Main** / **iosSimulatorArm64Main** | iOS `getAnchorContributors()` actual. KSP generates into target-specific dirs; you need actuals here, not `iosMain`. |
| **jvmMain** | Desktop/JVM-specific if needed (e.g. JVM-only modules). |
| **wasmJsMain** | Web/Wasm-specific if needed. |

**Important for iOS:** KSP generates code into `iosArm64Main` and `iosSimulatorArm64Main`, not `iosMain`. So you need an `actual` for `getAnchorContributors()` in **both** `iosArm64Main` and `iosSimulatorArm64Main`. See [Platform-Specific Setup](../installation/platform-specific).

---

## KSP Targets

Anchor DI uses **KSP (Kotlin Symbol Processing)** to generate code at compile time. You must add the KSP processor for **each Kotlin target** you use:

```kotlin
dependencies {
    add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:x.x.x")
    add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:x.x.x")
    add("kspIosArm64", "io.github.12345debdut:anchor-di-ksp:x.x.x")
    add("kspIosSimulatorArm64", "io.github.12345debdut:anchor-di-ksp:x.x.x")
    add("kspJvm", "io.github.12345debdut:anchor-di-ksp:x.x.x")
    add("kspWasmJs", "io.github.12345debdut:anchor-di-ksp:x.x.x")
}
```

- **kspCommonMainMetadata** — For shared code. Required if you have `commonMain` and use KSP there.
- **kspAndroid** — For Android target.
- **kspIosArm64** / **kspIosSimulatorArm64** — For iOS device and simulator.
- **kspJvm** — For JVM/Desktop target.
- **kspWasmJs** — For Web/Wasm target.

Add KSP only for the targets you actually use.

---

## Platform-Specific Dependencies

When you need platform-specific types (e.g. `Context` on Android, `UIApplication` on iOS), use **expect** / **actual**:

```kotlin
// commonMain — expect
expect class PlatformContext

// androidMain — actual
actual class PlatformContext(private val context: Context)

// iosMain (or iosArm64Main / iosSimulatorArm64Main) — actual
actual class PlatformContext(private val application: UIApplication)
```

Then provide them via a module with platform-specific sources:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object PlatformModule {
    @Provides
    @Singleton
    fun providePlatformContext(): PlatformContext = PlatformContext(/* platform-specific */)
}
```

Each platform has its own `PlatformModule` that provides the platform-specific `PlatformContext`. The shared code depends only on the `PlatformContext` interface; it doesn't know about `Context` or `UIApplication`.

---

## Next Steps

- **[expect / actual and Platform Modules](expect-actual)** — Deep dive into platform-specific DI
- **[Platform-Specific Setup](../installation/platform-specific)** — iOS KSP setup, Android init, and more
