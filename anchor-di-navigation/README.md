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

**Dispose when popped:** With Navigation 3 (or any back stack), use `NavScopeContainer(backStack, scopeKeyForEntry) { ... }` and put your `NavDisplay` and `NavigationScopedContent` inside the lambda. The framework observes the back stack and disposes scopes for entries that are removed (e.g. when the user pops), so ViewModels and scoped state are retained for entries still on the stack and released when they are popped.

**Non-Compose:** Call `NavigationScopeRegistry.getOrCreate` / `dispose` when entering/leaving a screen (e.g. SwiftUI, native UI).
