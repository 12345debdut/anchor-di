# KMP Without Compose

Use Anchor DI in a **Kotlin Multiplatform** project that does **not** use Compose for UI — for example, SwiftUI on iOS, Views on Android, or shared logic only. This page explains what works, what's different, and what to add. You don't need `anchor-di-compose`; you'll use `Anchor.inject<T>()` and manual scope management instead of `anchorInject()` and `viewModelAnchor()`.

---

## Dependencies

Add these dependencies to your shared module:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.12345debdut:anchor-di-api:0.1.0")
            implementation("io.github.12345debdut:anchor-di-core:0.1.0")
            implementation("io.github.12345debdut:anchor-di-presentation:0.1.0")  // Optional: NavigationScopeRegistry
            implementation("io.github.12345debdut:anchor-di-android:0.1.0")       // Optional: ActivityScope (Android)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosSimulatorArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
}
```

**What each does:**

- `anchor-di-api` + `anchor-di-core` — Core DI. Always required.
- `anchor-di-presentation` — `NavigationScopeRegistry` for navigation-scoped DI without Compose.
- `anchor-di-android` — `ActivityScope` and Android-specific helpers.

---

## What Works

| Feature | Usage |
|---------|--------|
| **Constructor injection** | `@Inject`, `@Module`, `@Provides`, `@Binds`, `@InstallIn(SingletonComponent::class)` |
| **Singleton scope** | `Anchor.inject<T>()` from root |
| **Custom scopes** | `Anchor.withScope(MyScope::class) { scope -> scope.get<T>() }` or `Anchor.scopedContainer(MyScope::class)` |
| **ViewModel scope** | `ViewModelScopeRegistry.getOrCreate(scopeKey)` → resolve from returned container; `ViewModelScopeRegistry.dispose(scopeKey)` when screen is gone. On Android, use `anchor-di-android`'s `getViewModelScope(owner, lifecycle)` or `viewModelScope()` for auto-dispose. |
| **ActivityScope (Android)** | `Anchor.withScope(ActivityScope::class) { ... }` or `Anchor.scopedContainer(ActivityScope::class)` |
| **Navigation scope** | `NavigationScopeRegistry.getOrCreate(scopeKey)` / `dispose(scopeKey)` |
| **Lazy / Provider** | `Lazy<T>`, `AnchorProvider<T>` injection |
| **Multibinding** | `@IntoSet`, `@IntoMap`, `Anchor.injectSet<T>()`, `Anchor.injectMap<V>()` |
| **KSP** | Same as with Compose: `kspCommonMainMetadata`, `kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64` |

---

## What's Different

| With Compose | Without Compose |
|--------------|-----------------|
| `anchorInject()` | Use `Anchor.inject<T>()` directly where you need the dependency (e.g. in a screen factory or service locator). |
| `viewModelAnchor()` | Use `Anchor.withScope(ViewModelComponent::class) { scope -> scope.get<ViewModel>() }` or hold a scoped container per screen and dispose it when leaving. |
| `NavigationScopedContent` + `navigationScopedInject()` | Use `NavigationScopeRegistry.getOrCreate(scopeKey)` when entering a screen and `dispose(scopeKey)` when leaving (e.g. in SwiftUI or native UI). |

---

## Summary

Core DI (api, core, ksp), scopes, and codegen work the same. The main difference is that you don't have Compose-specific helpers (`anchorInject()`, `viewModelAnchor()`). Use `Anchor.inject<T>()` and manual scope management instead. For ViewModel and navigation scopes, use `anchor-di-android` and `anchor-di-presentation` as needed. For ViewModel scope without Compose, use `ViewModelScopeRegistry.getOrCreate(scopeKey)` and `dispose(scopeKey)` when the screen is gone; on Android, use `anchor-di-android`'s `getViewModelScope(owner, lifecycle)` or `viewModelScope()` for auto-dispose.

---

## Next Steps

- **[Installation Setup](setup)** — Full installation guide
- **[Core Concepts](../core/concepts)** — Components and scopes
- **[Platform-Specific Setup](platform-specific)** — iOS KSP setup and more
