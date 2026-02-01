# ViewModel Support on All Platforms

Anchor DI supports **ViewModelComponent**-scoped bindings (one instance per “owner”) on all platforms, with or without Compose.

---

## Common API (all platforms)

Use **ViewModelScopeRegistry** (anchor-di-presentation, Compose-free):

- **Enter:** `ViewModelScopeRegistry.getOrCreate(scopeKey)` → `AnchorContainer` (ViewModel scope).
- **Leave:** `ViewModelScopeRegistry.dispose(scopeKey)` when the screen/owner is gone.

Resolve ViewModel-scoped types from the returned container:

```kotlin
val container = ViewModelScopeRegistry.getOrCreate(screenId)
val viewModel = container.get<MyViewModel>()
// When leaving the screen:
ViewModelScopeRegistry.dispose(screenId)
```

Use a **stable scope key** per screen/owner (e.g. route id, screen id, or the owner instance). Same key → same scope; dispose when the owner is destroyed so the scope is released.

---

## By platform

### Android (without Compose)

Add **anchor-di-android** and **anchor-di-presentation**. Use either:

**Option A — Owner + Lifecycle (recommended):** scope is disposed when the owner is destroyed.

```kotlin
import com.debdut.anchordi.android.getViewModelScope
import com.debdut.anchordi.android.viewModelScope

// In Activity (ComponentActivity):
class MainActivity : ComponentActivity() {
    private val viewModelScope by lazy { viewModelScope() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = viewModelScope.get<MainViewModel>()
    }
}

// In Fragment:
val container = getViewModelScope(this, viewLifecycleOwner.lifecycle)
val vm = container.get<MyViewModel>()
```

**Option B — Manual key:** use `ViewModelScopeRegistry.getOrCreate(scopeKey)` and `dispose(scopeKey)` in `onDestroy()`.

### Android (with Compose)

Use **anchor-di-compose**: `viewModelAnchor()` in a `@Composable` so the ViewModel is scoped to the Compose/ViewModelStoreOwner lifecycle. No need for ViewModelScopeRegistry or anchor-di-android ViewModel helpers unless you want the same lifecycle outside Compose.

### iOS / JVM / JS (no Compose)

Use **ViewModelScopeRegistry** with a key that identifies the screen/owner:

```kotlin
// When entering the screen (e.g. SwiftUI view appeared, or JVM/JS screen shown):
val scopeKey = screenId // or route, or ViewController id
val container = ViewModelScopeRegistry.getOrCreate(scopeKey)
val vm = container.get<MyViewModel>()

// When leaving the screen (e.g. view disappeared, back navigation):
ViewModelScopeRegistry.dispose(scopeKey)
```

You own the scope key and when to call `dispose`; the registry does not know about your UI lifecycle.

---

## With Compose Multiplatform

Use **viewModelAnchor()** (anchor-di-compose) so the ViewModel is created inside ViewModel scope and tied to the Compose/ViewModelStoreOwner. No manual registry or dispose.

---

## Summary

| Platform        | Without Compose                                      | With Compose        |
|----------------|--------------------------------------------------------|---------------------|
| **All**        | `ViewModelScopeRegistry.getOrCreate(key)` / `dispose(key)` | —                   |
| **Android**    | anchor-di-android: `getViewModelScope(owner, lifecycle)` or `viewModelScope()`; auto-dispose on destroy | `viewModelAnchor()` |
| **iOS / JVM / JS** | Same key + manual dispose when screen is gone          | `viewModelAnchor()` |

So: **ViewModel support is available on all platforms** via ViewModelScopeRegistry (and on Android via anchor-di-android helpers that tie the scope to Activity/Fragment lifecycle).
