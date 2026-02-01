---
sidebar_position: 1
---

# Kotlin Multiplatform Overview

Anchor DI is **multiplatform by design**. Generated code lives in `commonMain`; platform-specific dependencies use `expect / actual`.

## Source Sets

| Source Set | Use |
|------------|-----|
| `commonMain` | Shared DI graph, modules, `@Inject` classes |
| `androidMain` | Android-specific modules (e.g. `ActivityScope`) |
| `iosArm64Main` / `iosSimulatorArm64Main` | iOS `getAnchorContributors()` actual |
| `jvmMain` | Desktop/JVM-specific if needed |
| `wasmJsMain` | Web/Wasm-specific if needed |

## KSP Targets

Add KSP for each Kotlin target you use:

```kotlin
add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:0.1.0")
add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:0.1.0")
add("kspIosArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
add("kspIosSimulatorArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
add("kspJvm", "io.github.12345debdut:anchor-di-ksp:0.1.0")
add("kspWasmJs", "io.github.12345debdut:anchor-di-ksp:0.1.0")
```

## Platform-Specific Dependencies

Use `expect / actual` for platform-specific types:

```kotlin
// commonMain
expect class PlatformContext

// androidMain
actual class PlatformContext(private val context: Context)

// iosMain
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
