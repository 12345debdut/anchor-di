---
sidebar_position: 1
---

# Compose Multiplatform Overview

Anchor DI has **first-class support for Compose Multiplatform (CMP)**. If you're building UI with Compose on Android, iOS, Desktop, or Web, this page explains how to inject dependencies into your Composables and ViewModels in a clean, lifecycle-aware way.

---

## Why Compose-Specific APIs?

In imperative code, you typically call `Anchor.inject<T>()` at the point where you need the dependency. In Compose, the situation is different:

1. **Recomposition** — Composables can recompose many times. You want the injected instance to be **stable** across recomposition (not recreated on every recomposition).
2. **Lifecycle** — ViewModels have a lifecycle tied to the ViewModelStoreOwner. You need to resolve ViewModels inside the ViewModel scope, not at the root.

Anchor DI provides two main helpers for Compose:

- **`anchorInject()`** — For singletons or unscoped dependencies. Stable across recomposition.
- **`viewModelAnchor()`** — For ViewModels and ViewModel-scoped dependencies. Tied to the ViewModel lifecycle.

---

## anchorInject()

Use `anchorInject()` when you need a **singleton** or **unscoped** dependency in a Composable. The instance is resolved once (per composition) and cached across recomposition.

### Example

```kotlin
@Composable
fun SettingsScreen(
    repository: SettingsRepository = anchorInject()
) {
    // repository is resolved once per composition; stable across recomposition
    val settings by repository.settings.collectAsState(initial = null)
    // ...
}
```

**How it works:** Under the hood, `anchorInject()` uses `remember { Anchor.inject<T>() }`. So the instance is created on first composition and reused on subsequent recompositions. It's lifecycle-aware — if the Composable leaves composition and re-enters, you may get a new `remember` block, but for singletons you'll get the same cached instance from the root container.

**When to use:** For app-wide singletons (repositories, config, analytics) that you need in a Composable. Don't use it for ViewModel-scoped types — use `viewModelAnchor()` instead.

---

## viewModelAnchor()

Use `viewModelAnchor()` when you need an **@AnchorViewModel** ViewModel in a Composable. The ViewModel and its ViewModel-scoped dependencies are created inside the ViewModel scope, so everything is wired correctly.

### Example

```kotlin
@AnchorViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository  // ViewModel-scoped
) : ViewModel() {
    val uiState = /* ... */
    fun loadUser() { /* ... */ }
}

@Composable
fun UserScreen(
    viewModel: UserViewModel = viewModelAnchor()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ViewModel and UserRepository share the same scope; created once per screen
}
```

**How it works:** `viewModelAnchor()` uses Compose's `viewModel { }` under the hood and runs resolution inside `Anchor.withScope(ViewModelComponent::class)`. So the ViewModel and everything it injects (including ViewModel-scoped types like `UserRepository`) are created in the ViewModel scope. They share the same scope and lifecycle.

**Critical:** Use `viewModelAnchor()`, **not** `viewModel { Anchor.inject<ViewModel>() }`. If you use `viewModel { Anchor.inject<MyViewModel>() }`, resolution runs at the **root** (no ViewModel scope). ViewModel-scoped dependencies (e.g. a repository from `@InstallIn(ViewModelComponent::class)`) will fail with "Scoped binding for X requires a scope." Only `viewModelAnchor()` runs resolution inside the ViewModel scope.

---

## Platform Support

| Platform | anchorInject() | viewModelAnchor() |
|----------|----------------|-------------------|
| Android | ✅ | ✅ |
| iOS | ✅ | ✅ |
| Desktop (JVM) | ✅ | ✅ |
| Web (Wasm) | ✅ | ✅ |

Both work in **commonMain** — you write the same code everywhere.

---

## Lifecycle Summary

| API | Lifecycle | Use Case |
|-----|-----------|----------|
| `anchorInject()` | Resolved once per composition; for singletons, same instance across the app | App-wide dependencies (repositories, config) |
| `viewModelAnchor()` | Tied to ViewModelStoreOwner; cleared when the ViewModel is cleared | ViewModels and ViewModel-scoped dependencies |

---

## Quick Reference

```kotlin
// Singleton or unscoped — use anchorInject()
@Composable
fun SettingsScreen(
    config: AppConfig = anchorInject()
) { /* ... */ }

// ViewModel with ViewModel-scoped dependencies — use viewModelAnchor()
@Composable
fun UserScreen(
    viewModel: UserViewModel = viewModelAnchor()
) { /* ... */ }
```

---

## Next Steps

- **[Navigation-Scoped DI](navigation-scoped)** — Scoping objects to navigation destinations
- **[Built-in Scopes](../scopes/built-in)** — Deep dive into scopes and when to use each
