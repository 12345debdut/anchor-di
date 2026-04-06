# Changelog

All notable changes to Anchor DI will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
- **iOS thread safety:** `SyncLock` on Kotlin/Native now uses a real mutex via `kotlinx.atomicfu` instead of a no-op. Fixes potential data races when accessing the DI container from background coroutines on iOS under the new memory model.

### Added
- **Custom qualifier support:** `@Qualifier` meta-annotation is now fully functional. Define custom qualifiers with parameters (e.g., `@ApiKey("prod")`) or as markers (e.g., `@Production`), and the KSP processor will resolve them correctly on `@Provides`, `@Binds`, and `@Inject` sites.
- **Convention plugin:** `anchor-di-convention` precompiled Gradle script plugin for zero-config setup. Replaces ~12 lines of manual KSP wiring with a single plugin application.
- **Binary Compatibility Validator (BCV):** Public API changes are now tracked via `.api` files. CI will fail if public API is changed without updating the API dump.
- **API stability annotations:** `@ExperimentalAnchorApi` and `@InternalAnchorApi` opt-in annotations for marking unstable or internal surfaces.
- **ProGuard/R8 consumer rules:** All Android modules now ship consumer ProGuard rules to prevent R8 from stripping generated code or runtime classes.
- **CHANGELOG.md:** This file, following Keep a Changelog format.

### Changed
- `@Qualifier` meta-annotation is no longer experimental — custom qualifiers are fully supported by the KSP processor alongside `@Named`.
- `SyncLock` is no longer an `expect/actual` class. It uses `kotlinx.atomicfu.locks.SynchronizedObject` in common code, providing correct synchronization on all platforms.

## [0.1.0] - 2025-05-01

### Added
- Initial public release of Anchor DI.
- Compile-time dependency injection for Kotlin Multiplatform via KSP.
- Constructor injection with `@Inject`.
- Module system with `@Module`, `@InstallIn`, `@Provides`, `@Binds`.
- Scoping: `@Singleton`, `@ViewModelScoped`, `@NavigationScoped`, custom scopes via `@Scoped`.
- Qualifiers: `@Named` for disambiguating bindings.
- Lazy injection: `Lazy<T>` and `AnchorProvider<T>`.
- Multibinding: `@IntoSet` and `@IntoMap` with `@StringKey`.
- Compose Multiplatform integration: `anchorInject()`, `viewModelAnchor()`, `navigationScopedInject()`.
- ViewModel support: `@AnchorViewModel` with lifecycle-aware scoping.
- Navigation scoping: `NavScopeContainer`, `NavigationScopedContent` with automatic disposal.
- 15+ compile-time validators: missing bindings, cycles, scope violations, duplicate bindings.
- Multi-module support via `anchorDiModuleId` KSP option.
- Testing support: `Anchor.reset()` and contributor overrides.
- Platform targets: Android, iOS (arm64 + simulator), JVM, WasmJs.
- Published to Maven Central under `io.github.12345debdut`.

[Unreleased]: https://github.com/12345debdut/anchor-di/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/12345debdut/anchor-di/releases/tag/v0.1.0
