---
sidebar_position: 1
slug: /
---

# Welcome to Anchor DI

**Anchor DI** is a **compile-time dependency injection framework** for **Kotlin Multiplatform (KMP)** with first-class support for **Compose Multiplatform (CMP)**.

If you're building apps that run on Android, iOS, Desktop, and Web with shared Kotlin code — and you want clean, testable architecture — Anchor DI gives you a **Hilt/Dagger-like experience** that works everywhere. **If it compiles, it works.**

---

## What is Kotlin Multiplatform (KMP)?

Before diving into Anchor DI, let's understand the context. **Kotlin Multiplatform** lets you share business logic (networking, data layer, use cases) across Android, iOS, Desktop, and Web while keeping platform-specific UI and APIs where they belong. You write Kotlin once and run it everywhere — no JavaScript bridges, no duplicate codebases.

The challenge: **dependency injection** in KMP has been difficult. Most popular DI libraries either don't support KMP or have significant limitations. Anchor DI solves this.

---

## Why Anchor DI?

Dependency Injection in Kotlin Multiplatform is still a hard problem. Here's how existing solutions compare:

| Existing Solution | Limitation | Why It Matters |
|-------------------|------------|----------------|
| **Koin** | Runtime DI, slower startup, can fail at runtime | Your app might crash in production when a dependency is missing. Startup is slower because Koin builds the graph at launch. |
| **Hilt / Dagger** | Android-only | You can't use them in shared KMP code; iOS and other platforms are left out. |
| **Manual DI** | Boilerplate-heavy, error-prone | You write factory classes, pass dependencies through constructors manually — tedious and easy to get wrong. |
| **Reflection-based DI** | Not multiplatform-safe | Reflection is limited or unavailable on Kotlin/Native (iOS) and Kotlin/Wasm (Web). |

**Anchor DI solves this** by shifting all DI logic to **compile time**:

- **No runtime reflection** — Uses KSP (Kotlin Symbol Processing) to generate code at compile time
- **Fail fast** — Missing bindings, circular dependencies, and scope violations fail the build — you find issues before your app runs
- **Multiplatform** — Works in `commonMain`, `androidMain`, `iosMain`, `jvmMain`, `wasmJsMain`
- **Compose-first** — Designed for Compose Multiplatform from day one with `anchorInject()` and `viewModelAnchor()`

---

## A Quick Glimpse

Here's what Anchor DI looks like in practice:

```kotlin
// 1. Define a repository with constructor injection
@Singleton
class UserRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun loadUser(id: String) = api.getUser(id)
}

// 2. In Compose, inject a ViewModel — dependencies are resolved automatically
@Composable
fun UserScreen(
    viewModel: MainViewModel = viewModelAnchor()
) {
    // ViewModel and UserRepository are created and wired up by Anchor DI
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

You declare *what* you need; Anchor DI figures out *how* to provide it. No manual factories, no boilerplate.

---

## Design Principles

Anchor DI is built on a few core principles:

- **Compile-time dependency graph** — KSP analyzes your annotated code and generates factories and containers. No magic at runtime.
- **No Service Locator** — Dependencies are declared explicitly (constructors, modules). No hidden globals.
- **No runtime reflection** — Minimal runtime footprint; generated code does the work.
- **Strict validation** — Missing bindings, cycles, scope violations — all fail the build. Predictable behavior.
- **Multiplatform by design** — Same API on Android, iOS, Desktop, Web. Write once, run everywhere.

---

## What's Next?

Ready to get started? Here's the recommended path:

- **[Getting Started → Overview](getting-started/overview)** — Deep dive into what Anchor DI offers and how it fits into your KMP project
- **[Dependency Injection in a Nutshell](getting-started/dependency-injection)** — Understand DI concepts from scratch (great for beginners)
- **[Quick Example](getting-started/quick-example)** — A minimal setup you can copy and run
- **[Installation](installation/setup)** — Add Anchor DI to your KMP project step by step
- **[Core Concepts](core/concepts)** — Components, scopes, and how the dependency graph works
