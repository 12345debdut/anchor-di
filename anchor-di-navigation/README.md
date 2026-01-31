# anchor-di-navigation

Per-navigation-entry scope for Anchor DI. No Compose, no Nav library—just create/dispose scope for a key.

Use this module when you need **navigation-scoped** DI (one instance per destination) outside Compose, e.g. SwiftUI, native UI, or non-UI “screen” scope.

## Dependency

```kotlin
commonMain.dependencies {
    implementation(project(":anchor-di-api"))
    implementation(project(":anchor-di-runtime"))
    implementation(project(":anchor-di-navigation"))
}
```

## Usage

- **Enter:** `NavigationScopeRegistry.getOrCreate(scopeKey)` → `NavigationScopeEntry` with `navContainer` and `viewModelContainer`.
- **Leave:** `NavigationScopeRegistry.dispose(scopeKey)`.

Resolve from `entry.navContainer` or `entry.viewModelContainer` as needed.

**Compose:** This module includes Compose integration: `NavigationScopedContent`, `navigationScopedInject`, `navViewModelAnchor`. Add `anchor-di-navigation` when you need navigation-scoped DI in Compose. **anchor-di-compose** stays independent (viewModelAnchor, anchorInject only).

**Non-Compose:** Call `NavigationScopeRegistry.getOrCreate` / `dispose` when entering/leaving a screen (e.g. SwiftUI, native UI).
