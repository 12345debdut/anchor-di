# ⚓ Anchor DI

[![Maven Central](https://img.shields.io/maven-central/v/io.github.12345debdut/anchor-di-api)](https://central.sonatype.com/artifact/io.github.12345debdut/anchor-di-api)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**Compile-time dependency injection** for **Kotlin Multiplatform (KMP)** with first-class **Compose Multiplatform** support. Hilt/Dagger-like developer experience — if it compiles, it works.

---

## What is Anchor DI?

Anchor DI is a **compile-time** DI framework for KMP. It uses **KSP (Kotlin Symbol Processing)** to analyze your annotated code and generate a static dependency graph at build time. No runtime reflection, no service locator — just generated Kotlin code that wires your dependencies.

**Built for:** Android, iOS, Desktop (JVM), and Web (Wasm). Same API across all platforms.

---

## Why Anchor DI?

| Solution | Limitation |
|----------|------------|
| **Koin** | Runtime DI; slower startup; can fail at runtime ("No definition found") |
| **Hilt / Dagger** | Android-only |
| **Manual DI** | Boilerplate-heavy; error-prone |
| **Reflection-based** | Not multiplatform-safe (limited on Native, Wasm) |

**Anchor DI** validates your dependency graph at compile time — missing bindings, cycles, and scope violations fail the build, not your app.

---

## Quick Start

### 1. Add dependencies

```kotlin
// build.gradle.kts (shared module)
plugins {
    id("com.google.devtools.ksp") version "2.3.5"
}

repositories { mavenCentral() }

dependencies {
    implementation("io.github.12345debdut:anchor-di-api:0.1.0")
    implementation("io.github.12345debdut:anchor-di-core:0.1.0")
    implementation("io.github.12345debdut:anchor-di-compose:0.1.0")  // For Compose UI
    add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosSimulatorArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
}
```

### 2. Define and inject

```kotlin
@Singleton
class UserRepository @Inject constructor(
    private val api: UserApi
)

@AnchorViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel()

@Composable
fun UserScreen(
    viewModel: UserViewModel = viewModelAnchor()
) {
    // ViewModel and dependencies are injected automatically
}
```

### 3. Initialize at startup

```kotlin
Anchor.init(*getAnchorContributors())
```

Call once in `Application.onCreate()` (Android), your app entry (iOS), or before the first Composable.

---

## Installation Variants

| Setup | Dependencies |
|-------|--------------|
| **KMP + Compose** | `anchor-di-api`, `anchor-di-core`, `anchor-di-compose`, `anchor-di-ksp` |
| **KMP without Compose** | `anchor-di-api`, `anchor-di-core`, optionally `anchor-di-presentation`, `anchor-di-android` |
| **Multi-module** | Add `anchorDiModuleId` per module; aggregate contributors in your app |

**Prerequisites:** Kotlin 1.9+, KSP 2.3+, Gradle 8+

---

## Key Features

| Feature | Description |
|---------|-------------|
| **Constructor injection** | `@Inject` on constructors; dependencies resolved automatically |
| **Modules** | `@Module`, `@Provides`, `@Binds`, `@InstallIn` |
| **Scopes** | Singleton, ViewModel, Navigation, custom (`@Scoped`, `Anchor.withScope`) |
| **Compose** | `anchorInject()`, `viewModelAnchor()`, `navigationScopedInject()` |
| **Qualifiers** | `@Named`, custom `@Qualifier` |
| **Multibinding** | `@IntoSet`, `@IntoMap`, `Anchor.injectSet()`, `Anchor.injectMap()` |
| **Lazy / Provider** | `Lazy<T>`, `AnchorProvider<T>` |
| **Compile-time validation** | Missing bindings, cycles, scope violations fail the build |

---

## Platforms

| Platform | Status |
|----------|--------|
| Android | ✅ |
| iOS (arm64, simulator) | ✅ |
| Desktop (JVM) | ✅ |
| Web (Wasm) | ✅ |

---

## Documentation

**[Full documentation](https://12345debdut.github.io/anchor-di/)** — installation, core concepts, scopes, Compose integration, KMP setup, guides, and troubleshooting.

---

## Project Status

**Current version:** 0.1.0 (beta)

- Core DI, KSP validation, Compose & navigation integration
- Published to [Maven Central](https://central.sonatype.com/artifact/io.github.12345debdut/anchor-di-api)

---

## Contributing

Contributions welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) for how to build, run tests, and contribute.

---

## License

[Apache License 2.0](LICENSE)
