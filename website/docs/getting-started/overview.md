# Overview

## What is Anchor DI?

Anchor DI is a **compile-time** dependency injection framework for Kotlin Multiplatform. It uses **KSP (Kotlin Symbol Processing)** to analyze your annotated code and generate a static dependency graph during compilation.

### What gets validated at compile time

- Missing bindings
- Dependency cycles
- Scope violations
- Duplicate providers
- Invalid multibindings

Any violation fails the build — you find issues before your app runs.

## Key Features

| Feature | Description |
|---------|-------------|
| **Constructor injection** | `@Inject` on constructors; dependencies resolved automatically |
| **Modules** | `@Module` + `@InstallIn` to organize bindings |
| **Scopes** | Singleton, ViewModel, Navigation, and custom scopes |
| **Multibinding** | `@IntoSet` and `@IntoMap` for Dagger-style multibindings |
| **Compose integration** | `anchorInject()`, `viewModelAnchor()`, navigation-scoped injection |
| **KMP support** | commonMain, androidMain, iosMain; expect/actual for platform code |

## Architecture Overview

```
┌─────────────────┐     ┌──────────────┐     ┌─────────────────────┐
│  Your annotated │     │     KSP      │     │   Generated code    │
│  @Inject,       │ ──► │  processes   │ ──► │   Factories,        │
│  @Module code   │     │  at compile  │     │   Containers        │
└─────────────────┘     └──────────────┘     └─────────────────────┘
                                                       │
                                                       ▼
                                              ┌─────────────────────┐
                                              │  Anchor.init()      │
                                              │  Anchor.inject()    │
                                              └─────────────────────┘
```

## Artifacts

| Artifact | Purpose |
|----------|---------|
| `anchor-di-api` | Annotations only; no runtime dependency |
| `anchor-di-core` | Container, runtime resolution |
| `anchor-di-ksp` | KSP processor for code generation |
| `anchor-di-compose` | Compose helpers (anchorInject, viewModelAnchor) |
| `anchor-di-android` | ActivityScope, Android-specific helpers |
| `anchor-di-presentation` | NavigationScopeRegistry (Compose-free) |
