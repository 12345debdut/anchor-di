# Sample App (composeApp) — Testbed Guide

This document describes what the **composeApp** sample demonstrates and how to test each Anchor DI feature so you can validate the project for your needs.

---

## 1. What the Sample App Demonstrates

| Feature | Where to see it | How to test |
|--------|------------------|-------------|
| **Anchor.init + KSP** | Platform setup (`AnchorSetup.*.kt`) | Run the app on any target; if it starts, init and generated contributors work. |
| **SingletonComponent** | `AppModule`, `ProductApiModule`, `ProductApiBindsModule` | Singletons (e.g. `HttpClient`, `ProductApi`, `Platform`) are shared; no explicit UI, but product list loads from API. |
| **ViewModelComponent** | `RepositoryModule`, `ProductListViewModel`, `ProductDetailsViewModel` | ViewModels inject ViewModel-scoped repos; list/detail screens work and survive navigation. |
| **navViewModelAnchor()** | `ProductListScreen`, `ProductDetailsScreen` | Navigate list → detail → back; list state is preserved (ViewModel per navigation entry). |
| **NavigationScopedContent(entry)** | `ProductAppRoot.kt` | Each destination is wrapped in `NavigationScopedContent(entry)`; scope is disposed when destination is popped via [NavScopeContainer]. |
| **Custom component (SessionComponent)** | `SessionComponent`, `SessionModule`, `SessionState`, `SessionViewModel`, `SessionHolder` | Product list shows “Session: &lt;id&gt;” and a **Logout** button. Session is owned by `SessionViewModel` (no CompositionLocal); `SessionViewModel.logout()` disposes the session scope. See [SESSION_AND_LOGOUT.md](SESSION_AND_LOGOUT.md). |
| **anchorInject()** | `ProductListScreen` | Product list shows platform name via `anchorInject<Platform>()` (root-level singleton in Compose). |
| **viewModelAnchor()** | Not used (sample uses navViewModelAnchor) | Use when you have a ViewModelStoreOwner (e.g. Android Activity / Jetpack NavHost); see docs/VIEWMODEL_ANCHOR_PLAN.md. |
| **navigationScopedInject()** | Not yet used in UI | Add a type bound in `NavigationComponent` and resolve it with `navigationScopedInject<T>()` inside a destination to test. |

---

## 2. How to Run and Test

### 2.1 Run the app

- **Android**: Run `androidApp` or the Compose UI from your IDE; or `./gradlew :composeApp:compileDebugKotlinAndroid` then run the app.
- **Desktop (JVM)**: `./gradlew :composeApp:run`.
- **iOS**: Open `iosApp/` in Xcode and run; or use Kotlin task for the framework.
- **Web (Wasm)**: `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` (or equivalent).

If the app starts and you see the product list, the line **“Session: &lt;id&gt; · &lt;platform&gt;”**, and a **Logout** button, **Anchor.init**, **Singleton**, **ViewModel**, **Navigation scope**, **custom component** (session via ViewModel + logout), and **anchorInject()** are working.

### 2.2 Test navigation and ViewModel scope

1. Open the product list.
2. Tap a product → details screen.
3. Go back → list should still show the same data (ViewModel scoped to navigation entry).
4. Navigate again to another product → detail; back again. Each destination has its own ViewModel instance; list and detail state are independent per entry.

### 2.3 Test custom component (SessionComponent) and logout

1. On the product list screen, check the line **“Session: &lt;id&gt; · &lt;platform&gt;”** and the **Logout** button.
2. Session state comes from `SessionViewModel.getSessionState()`; the ViewModel delegates to `SessionHolder` (no CompositionLocal). See [SESSION_AND_LOGOUT.md](SESSION_AND_LOGOUT.md).
3. Tap **Logout**: the session scope is disposed (a new `Anchor.scopedContainer(SessionComponent::class)` is created). The session ID will change on the next read; all objects that were in the session component live until that point, then the new scope is used.

### 2.4 Test anchorInject() (root-level singleton)

- On the product list screen, the line under the app bar shows **“Session: &lt;id&gt; · &lt;platform&gt;”**. The platform name comes from `anchorInject<Platform>()`, confirming root-level singleton injection in Compose.

### 2.5 Optional: Test navigationScopedInject()

- Add a type bound in a module `@InstallIn(NavigationComponent::class)` (e.g. a `ScreenState` or helper).
- In a destination wrapped in `NavigationScopedContent`, call `navigationScopedInject<ThatType>()` and use it in the UI. This confirms Navigation-scoped resolution outside ViewModels.

---

## 3. Project layout (composeApp)

```
composeApp/src/commonMain/kotlin/com/debdut/simpletemplate/
├── App.kt                          # Root UI; provides LocalSessionContainer (custom scope)
├── di/
│   ├── AnchorSetup.kt              # expect getAnchorContributors()
│   ├── AppModule.kt                # SingletonComponent: Platform
│   ├── RepositoryModule.kt         # ViewModelComponent: GreetingRepository
│   ├── SessionComponent.kt         # Custom component (scope marker)
│   ├── SessionHolder.kt             # Holds session container; init(), logout()
│   ├── SessionModule.kt            # SessionComponent: SessionState
│   ├── SessionState.kt             # Session-scoped data
│   ├── SessionViewModel.kt         # getSessionState(), logout(); passed from root (no CompositionLocal)
│   └── ... (platform AnchorSetup.*.kt)
├── product/
│   ├── data/                       # Product, ProductApi, HttpClient (singleton)
│   ├── di/                         # ProductApiModule, ProductRepositoryModule
│   ├── navigation/                 # ProductAppRoot, routes, NavigationScopedContent
│   └── presentation/               # ProductListScreen, ProductDetailsScreen, ViewModels
├── theme/
└── ...
```

- **Singleton**: `AppModule`, `ProductApiModule` (and Binds), `ProductRepositoryModule` (ViewModel-scoped).
- **Navigation scope**: `ProductAppRoot` uses `NavScopeContainer<NavKey>` and wraps each destination with `NavigationScopedContent(entry)`.
- **Custom scope**: `SessionComponent` + `SessionModule` + `SessionState`; `SessionHolder` holds the scoped container; `SessionViewModel` exposes getSessionState() and logout() and is passed from `App` to `ProductAppRoot` to `ProductListScreen` (no CompositionLocal). See [SESSION_AND_LOGOUT.md](SESSION_AND_LOGOUT.md).

---

## 4. What is not demonstrated (yet)

You can extend the sample to cover:

- **viewModelAnchor()** (ViewModelStoreOwner-based; e.g. under Android Activity or Jetpack NavHost).
- **navigationScopedInject()** for a non-ViewModel type in a destination.
- **Qualifiers** (`@Named`) in the UI.
- **Lazy&lt;T&gt;** / **AnchorProvider&lt;T&gt;** injection.
- **ActivityScope** (Android-only).
- **Anchor.reset()** and test overrides (e.g. in unit or instrumented tests).
- **Multi-module KSP** (multiple `AnchorGenerated_*` contributors combined in `Anchor.init`).

Use this testbed to add those usages and verify behavior on the platforms you care about.
