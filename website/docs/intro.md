---
sidebar_position: 1
slug: /
---


# Welcome to Anchor DI

**Anchor DI** is a **compile-time dependency injection framework** for **Kotlin Multiplatform (KMP)** with first-class support for **Compose Multiplatform (CMP)**.

It brings a **Hilt / Dagger–like developer experience** to KMP while remaining:

- **Reflection-free** — No runtime reflection; everything is validated at compile time
- **Compile-time validated** — Missing bindings, cycles, and scope violations fail the build
- **Fully multiplatform** — Android, iOS, Desktop, Web
- **Compose-first** — Designed for Compose Multiplatform from day one

## Why Anchor DI?

Dependency Injection in Kotlin Multiplatform is still a hard problem.

| Existing Solution | Limitation |
|-------------------|------------|
| Koin | Runtime DI, slower startup, runtime failures |
| Hilt / Dagger | Android-only |
| Manual DI | Boilerplate-heavy, error-prone |
| Reflection-based DI | Not multiplatform-safe |

**Anchor DI solves this** by shifting all DI logic to compile time. If it compiles — it works.

## Quick Example

```kotlin
@Singleton
class UserRepository @Inject constructor(
    private val api: UserApi
)

@Composable
fun UserScreen(
    viewModel: MainViewModel = viewModelAnchor()
) {
    // ViewModel and its dependencies are injected automatically
}
```

## Design Principles

- **Compile-time dependency graph** — KSP analyzes your code and generates factories
- **No Service Locator** — Explicit dependency declaration
- **No runtime reflection** — Minimal runtime footprint
- **Strict validation** — Fail fast at build time
- **Multiplatform by design** — Common code, shared DI

## What's Next?

- **[Getting Started](/getting-started/overview)** — Learn what Anchor DI offers and how DI works
- **[Installation](/installation/setup)** — Add Anchor DI to your KMP project
- **[Core Concepts](/core/concepts)** — Components, scopes, and modules
