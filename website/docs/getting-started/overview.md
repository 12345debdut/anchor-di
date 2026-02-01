# Overview

This page gives you a solid understanding of what Anchor DI is, how it works under the hood, and what you get out of the box. If you're new to KMP or DI, read this before jumping into code.

---

## What is Anchor DI?

Anchor DI is a **compile-time** dependency injection framework for Kotlin Multiplatform. It uses **KSP (Kotlin Symbol Processing)** to analyze your annotated code during compilation and generate a **static dependency graph**. No reflection, no runtime graph building — just generated Kotlin code that wires your dependencies together.

Think of it like Dagger or Hilt, but designed from the ground up for KMP. Same annotations (`@Inject`, `@Module`, `@Provides`, `@Binds`), same mental model, same compile-time safety — but it runs on Android, iOS, Desktop, and Web.

---

## What Gets Validated at Compile Time?

One of the biggest advantages of compile-time DI is that many mistakes are caught *before* your app runs. Anchor DI validates the following during compilation:

| Validation | What It Catches |
|------------|-----------------|
| **Missing bindings** | You inject `UserRepository` but never provide it (no `@Inject` constructor, no `@Provides`, no `@Binds`). Build fails. |
| **Dependency cycles** | `A` depends on `B`, `B` depends on `C`, `C` depends on `A`. Build fails. |
| **Scope violations** | You try to resolve a ViewModel-scoped type from the root container (e.g. `Anchor.inject<UserRepository>()` when `UserRepository` is ViewModel-scoped). Build fails. |
| **Duplicate providers** | Two modules provide the same type with the same qualifier. Build fails. |
| **Invalid multibindings** | Duplicate keys in `@IntoMap`, invalid `@Binds` shapes, etc. Build fails. |

**If the build succeeds, your dependency graph is valid.** You won't see "No binding found" or "Scoped binding requires a scope" at runtime.

---

## Key Features

| Feature | Description | Use Case |
|---------|-------------|----------|
| **Constructor injection** | Put `@Inject` on constructors; Anchor DI resolves dependencies and creates instances. | Your own classes (repositories, use cases, ViewModels) |
| **Modules** | Use `@Module` and `@InstallIn` to organize bindings. `@Provides` for manual creation, `@Binds` for interface → implementation. | Third-party types, platform-specific types, complex construction |
| **Scopes** | Singleton (app-wide), ViewModel (per screen), Navigation (per destination), custom (your lifecycle). | Control object lifetime and avoid leaks |
| **Multibinding** | Contribute to `Set<T>` or `Map<String, V>` from multiple modules. | Analytics trackers, plugins, interceptors |
| **Compose integration** | `anchorInject()` and `viewModelAnchor()` for Composables. | Inject singletons or ViewModels in UI |
| **KMP support** | `commonMain`, `androidMain`, `iosMain`, `jvmMain`, `wasmJsMain`. Use `expect`/`actual` for platform code. | Shared logic across platforms |

---

## How Does It Work? (Architecture Overview)

Anchor DI follows a simple pipeline:

```
┌─────────────────────────────────────────────────────────────────────────┐
│  1. You write annotated code: @Inject, @Module, @Provides, @Binds, etc.  │
└─────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  2. KSP runs during compilation. It discovers your classes and modules,  │
│     validates the dependency graph, and generates a ComponentBinding     │
│     Contributor that registers all bindings.                             │
└─────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  3. At runtime, you call Anchor.init(*getAnchorContributors()). This     │
│     builds the container with all bindings.                              │
└─────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  4. You resolve dependencies: Anchor.inject<T>(), viewModelAnchor(),     │
│     anchorInject(), etc. The container uses the generated factories      │
│     to create instances.                                                 │
└─────────────────────────────────────────────────────────────────────────┘
```

**No reflection in the hot path.** Everything is generated code and direct calls.

---

## Artifacts (What to Add to Your Project)

Anchor DI is split into multiple artifacts so you only include what you need:

| Artifact | Purpose | When to Use |
|----------|---------|-------------|
| `anchor-di-api` | Annotations only (`@Inject`, `@Module`, `@Provides`, etc.). No runtime dependency. | Always — your code references these. |
| `anchor-di-core` | Container, runtime resolution, `Anchor` object. | Always — this is the runtime. |
| `anchor-di-ksp` | KSP processor. Generates code at compile time. | Always — needed for codegen. |
| `anchor-di-compose` | Compose helpers: `anchorInject()`, `viewModelAnchor()`, `NavigationScopedContent`, `navigationScopedInject()`. | When using Compose Multiplatform for UI. |
| `anchor-di-android` | `ActivityScope` and Android-specific helpers. | When you need Activity-scoped DI on Android (with or without Compose). |
| `anchor-di-presentation` | `NavigationScopeRegistry` (Compose-free). | When you use KMP without Compose but need navigation-scoped DI (e.g. SwiftUI, Views). |

**Typical setup for Compose Multiplatform:** `anchor-di-api` + `anchor-di-core` + `anchor-di-compose` + `anchor-di-ksp`.

---

## Next Steps

- **[Dependency Injection in a Nutshell](dependency-injection)** — Learn DI concepts from scratch (binding, scope, component)
- **[Quick Example](quick-example)** — A minimal setup you can run in minutes
