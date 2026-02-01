---
sidebar_position: 1
---

# Compose Multiplatform Overview

Anchor DI has **first-class Compose Multiplatform** support. Add `anchor-di-compose` for Compose-specific helpers.

## anchorInject()

Inject singleton or unscoped dependencies in a Composable:

```kotlin
@Composable
fun SettingsScreen(
    repository: SettingsRepository = anchorInject()
) {
    // repository is resolved once per composition, cached
}
```

## viewModelAnchor()

Inject an **@AnchorViewModel** ViewModel. The ViewModel and its ViewModel-scoped dependencies are created inside the ViewModel scope:

```kotlin
@AnchorViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository  // ViewModel-scoped
) : ViewModel()

@Composable
fun UserScreen(
    viewModel: UserViewModel = viewModelAnchor()
) {
    // ViewModel and UserRepository share the same scope
}
```

**Important:** Use `viewModelAnchor()`, not `viewModel { Anchor.inject<ViewModel>() }`. Only `viewModelAnchor()` runs resolution inside `ViewModelComponent` scope.

## Platforms

| Platform | anchorInject() | viewModelAnchor() |
|----------|----------------|-------------------|
| Android | ✅ | ✅ |
| iOS | ✅ | ✅ |
| Desktop (JVM) | ✅ | ✅ |
| Web (Wasm) | ✅ | ✅ |

## Lifecycle

- `anchorInject()` uses `remember { Anchor.inject<T>() }` — instance is stable across recomposition.
- `viewModelAnchor()` uses Compose's `viewModel { }` under the hood — tied to ViewModelStoreOwner lifecycle.
