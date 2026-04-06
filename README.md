# Anchor DI

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
| **Koin** | Runtime DI; [~5x slower startup](docs/BENCHMARKS.md); can fail at runtime ("No definition found") |
| **Hilt / Dagger** | Android-only |
| **Manual DI** | Boilerplate-heavy; error-prone |
| **Reflection-based** | Not multiplatform-safe (limited on Native, Wasm) |

**Anchor DI** validates your dependency graph at compile time — missing bindings, cycles, and scope violations fail the build, not your app.

---

## Quick Start

### Option A: Convention Plugin (Recommended)

```kotlin
// build.gradle.kts (shared module)
plugins {
    id("anchor-di-convention")
}

anchorDi {
    compose = true
}
```

### Option B: Manual Setup

```kotlin
// build.gradle.kts (shared module)
plugins {
    id("com.google.devtools.ksp") version "2.3.5"
}

repositories { mavenCentral() }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(platform("io.github.12345debdut:anchor-di-bom:1.0.0"))
            implementation("io.github.12345debdut:anchor-di-api")
            implementation("io.github.12345debdut:anchor-di-core")
            implementation("io.github.12345debdut:anchor-di-compose")  // For Compose UI
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:1.0.0")
    add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:1.0.0")
    add("kspIosArm64", "io.github.12345debdut:anchor-di-ksp:1.0.0")
    add("kspIosSimulatorArm64", "io.github.12345debdut:anchor-di-ksp:1.0.0")
}
```

### Define and inject

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

### Initialize at startup

```kotlin
Anchor.init(*getAnchorContributors())
```

Call once in `Application.onCreate()` (Android), your app entry (iOS), or before the first Composable.

---

## Installation Variants

| Setup | Dependencies |
|-------|--------------|
| **KMP + Compose** | `anchor-di-bom`, `anchor-di-api`, `anchor-di-core`, `anchor-di-compose`, `anchor-di-ksp` |
| **KMP without Compose** | `anchor-di-bom`, `anchor-di-api`, `anchor-di-core`, optionally `anchor-di-presentation`, `anchor-di-android` |
| **Multi-module** | Add `anchorDiModuleId` per module; aggregate contributors in your app |
| **Convention plugin** | Apply `anchor-di-convention` — handles KSP + deps automatically |

**Prerequisites:** Kotlin 1.9+, KSP 2.3+, Gradle 8+

---

## Key Features

| Feature | Description |
|---------|-------------|
| **Constructor injection** | `@Inject` on constructors; dependencies resolved automatically |
| **Modules** | `@Module`, `@Provides`, `@Binds`, `@InstallIn` |
| **Scopes** | Singleton, ViewModel, Navigation, custom (`@Scoped`, `Anchor.withScope`) |
| **Compose** | `anchorInject()`, `viewModelAnchor()`, `navigationScopedInject()` |
| **Qualifiers** | `@Named`, custom `@Qualifier` (including parameterized) |
| **Multibinding** | `@IntoSet`, `@IntoMap`, `Anchor.injectSet()`, `Anchor.injectMap()` |
| **Lazy / Provider** | `Lazy<T>`, `AnchorProvider<T>` |
| **Compile-time validation** | Missing bindings, cycles, scope violations fail the build |
| **~5x faster init vs Koin** | [Benchmarks](docs/BENCHMARKS.md) — compile-time codegen means near-zero startup cost |

---

## Platforms

| Platform | Status |
|----------|--------|
| Android | Supported |
| iOS (arm64, simulator) | Supported |
| Desktop (JVM) | Supported |
| Web (Wasm) | Supported |

---

## Documentation

**[Full documentation](https://12345debdut.github.io/anchor-di/)** — installation, core concepts, scopes, Compose integration, KMP setup, guides, and troubleshooting.

- [Koin Migration Guide](docs/MIGRATION_FROM_KOIN.md) — side-by-side comparison for teams moving from Koin
- [Benchmarks](docs/BENCHMARKS.md) — performance data: Anchor DI vs Koin
- [Changelog](CHANGELOG.md) — release history

---

## Contributing

Contributions welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) for how to build, run tests, and contribute.

---

## License

[Apache License 2.0](LICENSE)
