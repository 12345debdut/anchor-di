# ⚓ Anchor DI

**Anchor DI** is a compile-time dependency injection framework for **Kotlin Multiplatform (KMP)** with first-class support for **Compose Multiplatform (CMP)**. Hilt/Dagger-like experience — if it compiles, it works.

---

## Why Anchor DI?

| Traditional library | Limitation |
|--------------------|------------|
| **Koin** | Runtime DI, slower startup, runtime failures |
| **Hilt / Dagger** | Android-only |
| **Manual DI** | Boilerplate-heavy, error-prone |
| **Reflection-based** | Not multiplatform-safe |

**Anchor DI** shifts all DI logic to compile time — no reflection, no service locator, minimal runtime overhead.

---

## Installation

Add to your KMP project:

```kotlin
repositories { mavenCentral() }

dependencies {
    implementation("io.github.12345debdut:anchor-di-api:0.1.0")
    implementation("io.github.12345debdut:anchor-di-core:0.1.0")
    implementation("io.github.12345debdut:anchor-di-compose:0.1.0")  // For Compose
    add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosSimulatorArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
}
```

**Without Compose?** Use `anchor-di-api`, `anchor-di-core`, and optionally `anchor-di-presentation`, `anchor-di-android`. No Compose dependency.

---

## APIs & Features

| Feature | API / Annotation |
|---------|------------------|
| Constructor injection | `@Inject` |
| App-wide singleton | `@Singleton` |
| Per-ViewModel scope | `@ViewModelScoped`, `viewModelAnchor()` |
| Per-navigation scope | `@NavigationScoped`, `navigationScopedInject()` |
| Custom scope | `@Scoped(MyScope::class)`, `Anchor.withScope()` |
| Compose injection | `anchorInject()`, `viewModelAnchor()` |
| Modules | `@Module`, `@Provides`, `@Binds`, `@InstallIn` |
| Qualifiers | `@Named`, `@Qualifier` |
| Multibinding | `@IntoSet`, `@IntoMap`, `Anchor.injectSet()`, `Anchor.injectMap()` |
| Lazy / Provider | `Lazy<T>`, `AnchorProvider<T>` |

**Init:** `Anchor.init(*getAnchorContributors())` at app startup.

---

## Learn More

For guides, examples, and detailed usage: **[Documentation](https://12345debdut.github.io/anchor-di/)**

---

## Contributing & License

Contributions welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).

**License:** [Apache 2.0](LICENSE)
