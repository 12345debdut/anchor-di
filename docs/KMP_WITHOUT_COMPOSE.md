# Using Anchor DI in KMP Without Compose

Anchor DI has **first-class support for Compose Multiplatform (CMP)**. This document describes what works when you use Anchor DI in a **Kotlin Multiplatform (KMP)** project that does **not** use Compose for UI (e.g. SwiftUI on iOS, Views on Android, or shared logic only).

---

## What Works Out of the Box (KMP, No Compose)

You only need **anchor-di-api** and **anchor-di-core** (plus **anchor-di-ksp** for codegen). No Compose dependency.

| Feature | Usage |
|--------|--------|
| **Constructor injection** | `@Inject`, `@Module`, `@Provides`, `@Binds`, `@InstallIn(SingletonComponent::class)` |
| **Singleton scope** | Bindings in `SingletonComponent`; resolve with `Anchor.inject<T>()` from root |
| **Custom scopes** | `Anchor.withScope(MyScope::class) { container -> container.get<T>() }` or `Anchor.scopedContainer(scopeClass)` |
| **ViewModel scope (all platforms)** | `ViewModelScopeRegistry.getOrCreate(scopeKey)` → resolve from returned container; `ViewModelScopeRegistry.dispose(scopeKey)` when screen/owner is gone. On Android, use anchor-di-android's `getViewModelScope(owner, lifecycle)` or `viewModelScope()` for auto-dispose. See [VIEWMODEL_ALL_PLATFORMS.md](VIEWMODEL_ALL_PLATFORMS.md). |
| **Navigation scope (manual)** | `Anchor.scopedContainer(NavigationComponent.SCOPE_ID)` per screen; hold the reference and drop it when the screen is left |
| **Lazy / Provider** | `Lazy<T>`, `AnchorProvider<T>` injection |
| **Multibinding** | `@IntoSet`, `@IntoMap`, `Anchor.injectSet<T>()`, `Anchor.injectMap<V>()` |
| **KSP** | Use `kspCommonMainMetadata`, `kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64`, etc., as in the main README |

So: **core DI, scopes, and codegen are fully usable in KMP without Compose.** You call `Anchor.init(AnchorGenerated)` at app startup and use `Anchor.inject<T>()`, `Anchor.withScope(...)`, and `Anchor.scopedContainer(...)` from shared or platform code.

---

## Missing or Different Parts (KMP Without Compose)

### 1. **ViewModel scope lifecycle tied to platform “owner”**

- **With CMP:** `viewModelAnchor()` (anchor-di-compose) ties ViewModel scope to the Compose/ViewModelStoreOwner lifecycle; one scope per ViewModel, cleared when the ViewModel is cleared.
- **Without Compose:** There is no built-in “attach ViewModel scope to an Activity/Fragment/ViewController”. You can:
  - Use **block-scoped** ViewModel: `Anchor.withScope(ViewModelComponent.SCOPE_ID) { container -> container.get<MyScreenViewModel>() }` for the duration of a block (e.g. one screen’s logic).
  - Or **hold a scoped container per screen**: e.g. `val viewModelContainer = Anchor.scopedContainer(ViewModelComponent.SCOPE_ID)` when entering a screen, resolve ViewModels from it, and drop the reference when leaving the screen so the scope can be GC’d.

**Missing:** A small, optional API or documented pattern for “one ViewModel scope per Android Activity/Fragment” (and equivalent on other platforms) **without** pulling in Compose — e.g. `anchor-di-android` with something like `ViewModelScopeHolder.get(activity)` that creates/clears a `ViewModelComponent` scoped container tied to the Activity lifecycle.

### 2. **`anchorInject()` and `viewModelAnchor()`**

- **`anchorInject()`** — Compose-only (`remember { Anchor.inject<T>() }`). Without Compose, use **`Anchor.inject<T>()`** directly where you need the dependency (e.g. in a screen factory or service locator).
- **`viewModelAnchor()`** — Compose-only (uses `viewModel { }` + ViewModel scope). Without Compose, use **`Anchor.withScope(ViewModelComponent.SCOPE_ID) { it.get<T>() }`** or a scoped container as above.

No API is “missing” for core DI; only the Compose conveniences are not available.

### 3. **Navigation-scoped DI without Compose**

- **With CMP:** Add **anchor-di-navigation-compose** for `NavScopeContainer`, `NavigationScopedContent`, `navigationScopedInject`, `navViewModelAnchor`.
- **Without Compose:** Add **anchor-di-navigation** (Compose-free). It provides `NavigationScopeRegistry.getOrCreate(scopeKey)` and `NavigationScopeEntry` (navContainer, viewModelContainer). No Compose dependency. Call `getOrCreate(scopeKey)` when entering a screen and `NavigationScopeRegistry.dispose(scopeKey)` when leaving (e.g. SwiftUI, native UI).

### 4. **ActivityScope (Android, no Compose)**

- Add **anchor-di-android** (Compose-free, Android-only). It provides `ActivityScope` so you can use `Anchor.withScope(ActivityScope::class) { ... }` or `Anchor.scopedContainer(ActivityScope::class)` in KMP Android apps without Compose. **anchor-di-compose** (Android) depends on anchor-di-android and re-exports the same `ActivityScope` for CMP users.

### 5. **Documentation and samples**

- The main docs and sample (composeApp) are **Compose-oriented**. A short **“KMP without Compose”** quick start (deps: api + runtime + ksp; init; inject; withScope; scopedContainer; ViewModel/Navigation scope patterns) and, optionally, a minimal sample (e.g. shared module + Android View + iOS SwiftUI) would make the “no Compose” path obvious.

---

## Summary Table

| Area | KMP without Compose | Note |
|------|---------------------|------|
| Core DI (api, runtime, ksp) | ✅ Full | Use as-is. |
| Singleton, custom scopes | ✅ Full | `Anchor.inject`, `withScope`, `scopedContainer`. |
| ViewModel scope | ✅ Full | Use `ViewModelScopeRegistry.getOrCreate(scopeKey)` / `dispose(scopeKey)`; on Android use anchor-di-android's `getViewModelScope(owner, lifecycle)` or `viewModelScope()` for auto-dispose. |
| Navigation scope | ✅ Full | Add **anchor-di-navigation** (Compose-free): `NavigationScopeRegistry.getOrCreate(scopeKey)` / `dispose(scopeKey)`. |
| `anchorInject()` / `viewModelAnchor()` | ❌ N/A | Compose-only; use `Anchor.inject` and manual ViewModel scope. |
| ActivityScope (Android) | ✅ Full | Add **anchor-di-android** (Compose-free): `Anchor.withScope(ActivityScope::class) { ... }`. |
| Docs / samples | ✅ Guide | This doc; main README has a KMP install snippet. Optional: minimal KMP-only sample. |

**First-class KMP support:** Use **anchor-di-api**, **anchor-di-core**, **anchor-di-navigation**, and **anchor-di-android** (Android) for full DI and scopes without any Compose dependency. Use **anchor-di-compose** and **anchor-di-navigation-compose** when you adopt Compose Multiplatform.
