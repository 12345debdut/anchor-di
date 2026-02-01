# anchor-di-navigation

Per-navigation-entry scope for Anchor DI. No Compose, no Nav library—just create/dispose scope for a key.

Use this module when you need **navigation-scoped** DI (one instance per destination) outside Compose, e.g. SwiftUI, native UI, or non-UI “screen” scope.

## Dependency

```kotlin
commonMain.dependencies {
    implementation(project(":anchor-di-api"))
    implementation(project(":anchor-di-core"))
    implementation(project(":anchor-di-navigation"))
}
```

## Usage

- **Enter:** `NavigationScopeRegistry.getOrCreate(scopeKey)` → `NavigationScopeEntry` with `navContainer` and `viewModelContainer`.
- **Leave:** `NavigationScopeRegistry.dispose(scopeKey)`.

Resolve from `entry.navContainer` or `entry.viewModelContainer` as needed.

**ViewModel scope only (all platforms):** Use `ViewModelScopeRegistry.getOrCreate(scopeKey)` to get a ViewModel-scoped container; call `ViewModelScopeRegistry.dispose(scopeKey)` when the screen/owner is gone. See [docs/VIEWMODEL_ALL_PLATFORMS.md](../docs/VIEWMODEL_ALL_PLATFORMS.md) for ViewModel support on all platforms (Android, iOS, JVM, JS) with or without Compose.

**Compose:** For Compose Multiplatform, add **anchor-di-navigation-compose** (depends on this module). It provides `NavScopeContainer`, `NavigationScopedContent`, `navigationScopedInject`, `navViewModelAnchor`. **anchor-di-compose** stays independent (viewModelAnchor, anchorInject only).

**Dispose when popped (CMP):** With Navigation 3 (or any back stack), use `NavScopeContainer(backStack, scopeKeyForEntry) { ... }` from **anchor-di-navigation-compose** and put your `NavDisplay` and `NavigationScopedContent` inside the lambda.

**Non-Compose:** Call `NavigationScopeRegistry.getOrCreate` / `dispose` when entering/leaving a screen (e.g. SwiftUI, native UI).
