# Changelog

All notable changes to Anchor DI will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-04-06

### Fixed
- **iOS thread safety:** `SyncLock` on Kotlin/Native now uses a real mutex via `kotlinx.atomicfu` instead of a no-op. Fixes potential data races when accessing the DI container from background coroutines on iOS under the new memory model.

### Added
- Initial public release of Anchor DI.
- Compile-time dependency injection for Kotlin Multiplatform via KSP.
- Constructor injection with `@Inject`.
- Module system with `@Module`, `@InstallIn`, `@Provides`, `@Binds`.
- Scoping: `@Singleton`, `@ViewModelScoped`, `@NavigationScoped`, custom scopes via `@Scoped`.
- Qualifiers: `@Named` for disambiguating bindings.
- **Custom qualifier support:** `@Qualifier` meta-annotation is fully functional. Define custom qualifiers with parameters (e.g., `@ApiKey("prod")`) or as markers (e.g., `@Production`), and the KSP processor will resolve them correctly on `@Provides`, `@Binds`, and `@Inject` sites.
- Lazy injection: `Lazy<T>` and `AnchorProvider<T>`.
- Multibinding: `@IntoSet` and `@IntoMap` with `@StringKey`.
- Compose Multiplatform integration: `anchorInject()`, `viewModelAnchor()`, `navigationScopedInject()`.
- ViewModel support: `@AnchorViewModel` with lifecycle-aware scoping.
- Navigation scoping: `NavScopeContainer`, `NavigationScopedContent` with automatic disposal.
- 15+ compile-time validators: missing bindings, cycles, scope violations, duplicate bindings.
- Multi-module support via `anchorDiModuleId` KSP option.
- Testing support: `Anchor.reset()` and contributor overrides.
- Platform targets: Android, iOS (arm64 + simulator), JVM, WasmJs.
- **Convention plugin:** `anchor-di-convention` precompiled Gradle script plugin for zero-config setup. Replaces ~12 lines of manual KSP wiring with a single plugin application.
- **Gradle BOM:** `anchor-di-bom` module (`java-platform`) for version alignment. Consumers can use `implementation(platform("...:anchor-di-bom:1.0.0"))` to omit versions on individual modules.
- **Binary Compatibility Validator (BCV):** Public API changes tracked via `.api` files. CI fails if public API is changed without updating the API dump.
- **API stability annotations:** `@ExperimentalAnchorApi` and `@InternalAnchorApi` opt-in annotations for marking unstable or internal surfaces.
- **ProGuard/R8 consumer rules:** All Android modules ship consumer ProGuard rules to prevent R8 from stripping generated code or runtime classes.
- **Benchmarks vs Koin:** `benchmarks/` module using kotlinx-benchmark (JMH). Anchor DI initializes ~5x faster than Koin across 10/100/500 bindings. Resolution speed is comparable.
- **Dokka API documentation:** Dokka plugin configured for all library modules. Run `./gradlew dokkaHtmlMultiModule` to generate HTML API docs.
- **Koin migration guide:** Comprehensive `docs/MIGRATION_FROM_KOIN.md` covering all major patterns: singletons, factories, interface bindings, qualifiers, ViewModel scoping, multibinding, custom scopes, testing, and Compose integration.
- **End-to-end integration tests:** `anchor-di-integration-tests` module with 12 tests verifying the full runtime pipeline.
- **Presentation module tests:** `NavigationScopeRegistryTest` (11 tests) and `ViewModelScopeTest` (7 tests).
- **Kover coverage expansion:** `anchor-di-presentation` added to aggregated code coverage reporting.
- **SECURITY.md:** Vulnerability disclosure policy.
- **GitHub issue templates:** Bug report and feature request templates.
- **Website documentation:** Convention plugin setup, BOM usage, benchmarks, Koin migration guide, parameterized custom qualifiers.

### Changed
- `SyncLock` uses `kotlinx.atomicfu.locks.SynchronizedObject` in common code (no longer `expect/actual`), providing correct synchronization on all platforms.
- `SyncLock` marked `@InternalAnchorApi` — internal implementation detail, not part of the public API contract.

[Unreleased]: https://github.com/12345debdut/anchor-di/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/12345debdut/anchor-di/releases/tag/v1.0.0
