# Anchor DI v0.1.0 — First Release

**Compile-time dependency injection for Kotlin Multiplatform**

Anchor DI is a compile-time DI framework for Kotlin Multiplatform (KMP) with first-class Compose Multiplatform support. It uses KSP to generate a static dependency graph at build time: missing bindings, cycles, and scope violations fail the build instead of at runtime.

---

## Highlights

- **Compile-time safety** — No runtime reflection; dependency graph is validated by KSP.
- **Kotlin Multiplatform** — Shared code on Android, iOS, JVM, and Wasm with a single API.
- **Compose Multiplatform** — `anchorInject()`, `viewModelAnchor()`, and navigation-scoped injection.
- **Familiar API** — Hilt/Dagger-style annotations: `@Inject`, `@Module`, `@Provides`, `@Binds`, `@Singleton`, qualifiers, and custom scopes.

---

## Published Artifacts

| Artifact | Description |
|----------|-------------|
| `anchor-di-api` | Annotations (`@Inject`, `@Module`, `@Provides`, `@Binds`, `@Singleton`, etc.). |
| `anchor-di-core` | Runtime container and `Anchor` entry point. |
| `anchor-di-ksp` | KSP processor (add per target: `kspCommonMainMetadata`, `kspAndroid`, `kspIosArm64`, etc.). |
| `anchor-di-compose` | Compose integration: `anchorInject()`, `viewModelAnchor()`, `NavigationScopedContent`, `navigationScopedInject()`. |
| `anchor-di-android` | Android-specific: `ActivityScope`, `ActivityScope`-scoped bindings. |
| `anchor-di-presentation` | Navigation-scoped registry and shared presentation utilities. |

All artifacts are available on **Maven Central** under `io.github.12345debdut`.

---

## Features in This Release

- **Constructor injection** — `@Inject` on constructors; KSP-generated wiring.
- **Scopes** — `@Singleton` and custom scopes via `@Scoped(Scope::class)` with component dependencies.
- **Modules** — `@Module`, `@InstallIn(SingletonComponent::class)` (and other components), `@Provides`, `@Binds`.
- **Qualifiers** — `@Named` and custom qualifiers for disambiguation.
- **Lazy and providers** — `Lazy<T>` and `AnchorProvider<T>` injection.
- **Multibinding** — `@IntoSet` and `@IntoMap` with `@StringKey`.
- **ViewModel support** — `@AnchorViewModel` and `viewModelAnchor()` on Android and Compose.
- **Navigation scope** — `NavigationScopedContent` and `navigationScopedInject()` for per-destination DI.
- **Testing** — `Anchor.reset()` and contributor overrides for test doubles.
- **Multi-module** — Cross-module resolution with KSP option `anchorDiModuleId`.

---

## Requirements

- Kotlin 2.x
- KSP 2.3+
- Gradle 8+
- Compose Multiplatform (optional, for Compose integration)

---

## Documentation

- [README](https://github.com/12345debdut/anchor-di#readme) — Quick start and overview
- [Publishing](docs/PUBLISHING.md) — Releasing to Maven Central
- [Design](docs/DESIGN.md) — Architecture and design notes
- [Contributing](CONTRIBUTING.md) — How to contribute

---

## License

Apache License 2.0

---

*This is the first public release. We welcome feedback and contributions.*
