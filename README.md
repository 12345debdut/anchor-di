# ⚓ Anchor-DI

**Anchor-DI** is a **compile-time dependency injection framework for Kotlin Multiplatform (KMP)** with first-class support for **Compose Multiplatform (CMP)**.

It brings a **Hilt / Dagger–like developer experience** to KMP while remaining:
- 🚫 Reflection-free
- ⚡ Compile-time validated
- 🌍 Fully multiplatform (Android, iOS, Desktop, Web)
- 🎨 Compose-first

---

## ✨ Why Anchor-DI?

Dependency Injection in Kotlin Multiplatform is still a hard problem.

| Existing Solution | Limitation |
|------------------|------------|
| Koin | Runtime DI, slower startup, runtime failures |
| Hilt / Dagger | Android-only |
| Manual DI | Boilerplate-heavy, error-prone |
| Reflection-based DI | Not multiplatform-safe |

**Anchor-DI solves this by shifting all DI logic to compile time.**

---

## 🎯 Design Principles

- **Compile-time dependency graph**
- **No Service Locator**
- **No runtime reflection**
- **Strict validation**
- **Predictable behavior**
- **Multiplatform by design**

If it compiles — it works.

---

## 🧱 High-Level Architecture

Anchor-DI uses **KSP (Kotlin Symbol Processing)** to analyze your source code and generate a **static dependency graph** during compilation.

### What gets validated at compile time:
- Missing bindings
- Dependency cycles
- Scope violations
- Duplicate providers
- Invalid multibindings

💥 Any violation fails the build.

---

## 🌍 Multiplatform First

- Generated code lives in `commonMain`
- No JVM-only APIs
- Platform-specific dependencies use `expect / actual`

```kotlin
expect class PlatformContext
```

```kotlin
@Module
object PlatformModule {
    @Provides
    fun providePlatformContext(): PlatformContext
}
```

---

## 🧩 Core Concepts

### Constructor Injection

```kotlin
class UserRepository @Inject constructor(
    private val api: UserApi
)
```
---

### Components

Components are **entry points** into the dependency graph. You install modules in a component with `@InstallIn(Component::class)`; the component determines the scope and lifetime of bindings.

- **SingletonComponent** — application-wide; bindings live for the app lifetime.
- **ViewModelComponent** — one instance per ViewModel; use with `viewModelAnchor()`.
- **NavigationComponent** — one instance per navigation destination (Compose); use with `NavigationScopedContent` and `navigationScopedInject()`.
- **Custom components** — any top-level `object` or class; enter with `Anchor.withScope(MyScope::class)` or `Anchor.scopedContainer(MyScope::class)`.

---

This section walks through the main patterns: **SingletonComponent** for app-wide objects (e.g. HTTP client), **ViewModelComponent** for screen-level objects (e.g. repository), **@AnchorViewModel** and **viewModelAnchor()** for Compose, and **custom components** for your own scopes and lifecycle.

### 1. Using SingletonComponent for HttpClient

App-wide singletons (HTTP client, platform context, config) should be provided by modules installed in **SingletonComponent**. One instance is shared for the whole app.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
}
```

Any type requested from the root container (e.g. `Anchor.inject<HttpClient>()`) or from a child scope will get this same instance.

---

### 2. Using ViewModelComponent for Repository

Bindings that should live **per screen / per ViewModel** (e.g. a repository or use case used only by one screen) belong in **ViewModelComponent**. Install the module with `@InstallIn(ViewModelComponent::class)`.

```kotlin
@Module
@InstallIn(ViewModelComponent::class)
interface RepositoryModule {

    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
```

```kotlin
class UserRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient  // from SingletonComponent
) : UserRepository { ... }
```

One instance of `UserRepository` is created per ViewModel and reused for that ViewModel’s lifetime. Do **not** resolve ViewModel-scoped types from the root; only resolve them inside the ViewModel scope (see below).

---

### 3. Injecting Repository into ViewModel with @AnchorViewModel

ViewModels that need ViewModel-scoped dependencies (like the repository above) must be created **inside** the ViewModel scope. Mark the class with **@AnchorViewModel** and use constructor **@Inject**; Anchor-DI will resolve dependencies when the ViewModel is created via `viewModelAnchor()`.

```kotlin
@AnchorViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun loadUser() { ... }
}
```

**Important:** Create this ViewModel only with **viewModelAnchor()** (see next step). Do **not** use `viewModel { Anchor.inject<MainViewModel>() }` — that would resolve from the root and fail for ViewModel-scoped dependencies.

---

### 4. Injecting ViewModel into a Composable with viewModelAnchor()

In Compose, obtain an **@AnchorViewModel** ViewModel with **viewModelAnchor()**. It runs resolution inside `ViewModelComponent` so the ViewModel and its ViewModel-scoped dependencies (e.g. repository) are created in the correct scope.

```kotlin
@Composable
fun UserScreen(
    viewModel: MainViewModel = viewModelAnchor()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

For non-ViewModel dependencies (e.g. a singleton repository used directly in the UI), use **anchorInject()**:

```kotlin
@Composable
fun SettingsScreen(
    repository: SettingsRepository = anchorInject()
) {
    // ...
}
```

Both `viewModelAnchor()` and `anchorInject()` work in **commonMain** (Android, iOS, Desktop).

---

### 5. Navigation-scoped objects (Compose)

For **Compose Navigation** (e.g. Jetpack Navigation Compose on Android), you can scope objects to a **navigation destination**: one instance per destination, cleared when the destination is popped from the back stack. Use **NavigationComponent** and the compose helpers.

**1. Define navigation-scoped bindings:**

```kotlin
@NavigationScoped
class ScreenState @Inject constructor() { ... }

@Module
@InstallIn(NavigationComponent::class)
object DestinationModule {
    @Provides
    fun provideDestinationHelper(): DestinationHelper = DestinationHelperImpl()
}
```

**2. Wrap destination content and inject:**

On Android, wrap each NavHost destination with `NavigationScopedContent(navBackStackEntry)` and use `navigationScopedInject<T>()` inside it:

```kotlin
NavHost(navController, startDestination = "home") {
    composable("home") {
        NavigationScopedContent(requireNotNull(it)) {
            val state = navigationScopedInject<ScreenState>()
            val helper = navigationScopedInject<DestinationHelper>()
            HomeScreen(state, helper)
        }
    }
}
```

`NavigationScopedContent` creates one NavigationComponent scope per `NavBackStackEntry`; when the user navigates away and the destination is popped, the scope is released. Use **navigation-scoped** for state or services that should live as long as the destination (e.g. screen-level cache, destination-specific analytics). For ViewModel-scoped dependencies, keep using `viewModelAnchor()` inside the same destination.

---

### 6. Custom Components and Custom Scoping

**When to use:** SingletonComponent (app lifetime) and ViewModelComponent (ViewModel lifetime) cover most cases. Use a **custom component** when you need a scope that matches *your* lifecycle — e.g. one instance per Activity, per screen, or per user session — and you want to control when that scope starts and ends.

**What it is:** A custom component is a **scope marker**: a top-level `object` or `class` that you use as the scope. Bindings installed in that scope are created once per scope instance and live until the scope is disposed. You decide when to enter the scope and when to leave it.

---

#### Step 1: Define the scope marker

Use a top-level `object` (or class). It has no methods; it only identifies the scope. The type must be **top-level** so KSP and runtime agree on the scope ID.

```kotlin
object ActivityScope   // e.g. one scope per Android Activity
```

---

#### Step 2: Provide bindings for that scope

**Option A — Module with `@InstallIn(YourScope::class)`:** Types provided in this module are available only when that scope is active. They are created once per scope and cached for the scope’s lifetime.

```kotlin
@Module
@InstallIn(ActivityScope::class)
object ActivityModule {

    @Provides
    fun provideNavigator(): ScreenNavigator = ScreenNavigatorImpl()
}
```

**Option B — `@Scoped(YourScope::class)` on a class:** The class is created once per scope when requested inside that scope.

```kotlin
@Scoped(ActivityScope::class)
class ActivityScopedAnalytics @Inject constructor(
    private val navigator: ScreenNavigator  // from ActivityModule above
)
```

---

#### Step 3: Enter the scope and resolve dependencies

You must **enter** the scope before resolving any type that is bound to that scope. Two patterns:

**Pattern A — Temporary scope (`withScope`):** The scope exists only for the duration of the block. When the block exits, the scope ends and cached instances are no longer used. Good for one-off work (e.g. a single function that needs activity-scoped types).

```kotlin
Anchor.withScope(ActivityScope::class) { scope ->
    val navigator = scope.get<ScreenNavigator>()
    val analytics = scope.get<ActivityScopedAnalytics>()
    // use them; when the block ends, the scope is done
}
```

**Pattern B — Long-lived scope (`scopedContainer`):** You get a container that *is* the scope. You keep a reference to it; the scope lives as long as you hold that reference. When your Activity (or screen/session) is destroyed, stop using the container and let it go out of scope. Good when the scope must outlive a single block (e.g. an Activity that injects activity-scoped types in multiple places).

```kotlin
// e.g. in Activity.onCreate()
val activityScope = Anchor.scopedContainer(ActivityScope::class)

// Later, anywhere you have activityScope:
val navigator = activityScope.get<ScreenNavigator>()

// When Activity is destroyed, stop holding activityScope so it can be GC'd
```

---

#### Rules and gotchas

| Rule | Why |
|------|-----|
| Custom scope type must be **top-level** | KSP and runtime use the type’s qualified name as the scope ID; only top-level types have a stable, matching name. |
| Resolve scoped types **only** inside `withScope { }` or from a `scopedContainer(...)` | If you call `Anchor.inject<ActivityScopedAnalytics>()` from the root (no scope), you get *"Scoped binding for X requires a scope"*. |
| You own the lifecycle | Nothing automatically creates or destroys the scope; you call `withScope` or hold/release `scopedContainer`. |

---

## 🔐 Scoping Model

**What is a scope?** A scope controls **how long an instance lives** and **how many instances exist**. When you request a type, the container looks at its binding: unscoped (new each time), singleton (one for the app), or scoped (one per scope). Scoping lets you share state within a boundary (e.g. one repository per ViewModel) and avoid leaking long-lived objects into shorter-lived ones.

---

### Binding kinds

| Kind | Annotation | Lifetime | Where to resolve |
|------|------------|----------|------------------|
| **Unscoped** | *(none)* | New instance on every request | Anywhere (root or inside a scope). |
| **Singleton** | `@Singleton` | One instance for the whole app | Anywhere; cached at the root. |
| **ViewModel-scoped** | `@ViewModelScoped` or `@InstallIn(ViewModelComponent::class)` | One instance per ViewModel | Only inside ViewModel scope (e.g. via `viewModelAnchor()`). |
| **Navigation-scoped** | `@NavigationScoped` or `@InstallIn(NavigationComponent::class)` | One instance per navigation destination | Only inside navigation scope (e.g. `NavigationScopedContent` + `navigationScopedInject()`). |
| **Custom-scoped** | `@Scoped(MyScope::class)` or `@InstallIn(MyScope::class)` | One instance per scope instance | Only inside that scope (`Anchor.withScope(MyScope::class) { }` or `Anchor.scopedContainer(MyScope::class)`). |

Unscoped and singleton bindings are resolved from the **root** container. Scoped bindings are resolved only when the current resolution is happening **inside** the right scope; otherwise you get *"Scoped binding for X requires a scope"*.

---

### How scope is determined at runtime

1. **Root (no scope):** `Anchor.inject<T>()` or `container.get<T>()` on the root container. Only **unscoped** and **singleton** bindings can be resolved. Any **scoped** binding throws.

2. **Inside a scope:** When you run code inside `Anchor.withScope(Scope::class) { scope -> scope.get<T>() }` or from a container returned by `Anchor.scopedContainer(Scope::class)`, that container has a **current scope ID**. A binding scoped to that same scope is created once per scope and cached in that container. Bindings from **parent** scopes (e.g. singleton) are still visible: the container delegates to its parent when the requested binding is not for the current scope.

3. **ViewModel scope:** ViewModels created with `viewModelAnchor()` are created inside `ViewModelComponent` scope. So the ViewModel and everything it injects (including ViewModel-scoped types like a repository from a module `@InstallIn(ViewModelComponent::class)`) see the same scope. If you create the ViewModel with `viewModel { Anchor.inject<MyViewModel>() }`, resolution runs at the root and ViewModel-scoped dependencies fail.

4. **Navigation scope:** When content is wrapped in `NavigationScopedContent(navBackStackEntry)` (Android), that composable provides a container with `NavigationComponent` scope. Calls to `navigationScopedInject<T>()` inside that content resolve from that container, so Navigation-scoped bindings work. Outside that content, `LocalNavigationScope` is null and `navigationScopedInject()` throws.

---

### Annotations in practice

- **`@Singleton`** — Use for app-wide singletons (HTTP client, database, config). Install the module in `SingletonComponent` or put `@Singleton` on the `@Provides` method or `@Inject` class.

- **`@ViewModelScoped`** — Use when the type should live as long as a single ViewModel (e.g. screen state, or a repository used only by that screen). Same effect as installing a module in `ViewModelComponent::class`. Resolve only inside ViewModel scope (e.g. inject into an `@AnchorViewModel` class obtained via `viewModelAnchor()`).

- **`@NavigationScoped`** — Use when the type should live as long as a navigation destination (e.g. destination-level state or helper). Same effect as installing a module in `NavigationComponent::class`. Resolve only inside navigation scope (wrap content in `NavigationScopedContent` and use `navigationScopedInject()`).

- **`@Scoped(MyScope::class)`** — Use for custom scopes. One instance per scope; resolve only inside `Anchor.withScope(MyScope::class) { }` or from `Anchor.scopedContainer(MyScope::class)`.

Example: ViewModel-scoped repository and screen state:

```kotlin
@ViewModelScoped
class ScreenState @Inject constructor() { ... }

@Module
@InstallIn(ViewModelComponent::class)
interface RepositoryModule {
    @Binds
    fun bindRepo(impl: UserRepositoryImpl): UserRepository
}

@AnchorViewModel
class HomeViewModel @Inject constructor(
    private val repo: UserRepository,
    private val screenState: ScreenState
) : ViewModel()
```

`HomeViewModel` is created via `viewModelAnchor()`, so resolution runs inside ViewModel scope; `UserRepository` and `ScreenState` are both one instance per ViewModel.

---

### Rules to avoid scope errors

| Rule | Reason |
|------|--------|
| Resolve ViewModel-scoped types only inside ViewModel scope | Use `viewModelAnchor()` for ViewModels that need them; do not use `Anchor.inject<ViewModel>()` or `viewModel { Anchor.inject<...>() }`. |
| Resolve navigation-scoped types only inside navigation scope | Use `NavigationScopedContent(navBackStackEntry)` and `navigationScopedInject()`; do not call `Anchor.inject<...>()` for Navigation-scoped types from the root. |
| Resolve custom-scoped types only inside that scope | Use `Anchor.withScope(MyScope::class) { }` or hold a container from `Anchor.scopedContainer(MyScope::class)`. |
| Do not inject a scoped type into a longer-lived type | e.g. Do not inject a ViewModel-scoped type into a singleton; the singleton outlives the ViewModel and would hold a stale scope. The dependency graph is validated at compile time where possible. |

---

## 🔀 Multibinding

Supports **Set** and **Map** multibindings (Dagger-style).

### Into Set

```kotlin
@IntoSet
@Provides
fun provideAnalyticsTracker(): Tracker
```

### Into Map

```kotlin
@IntoMap
@StringKey("firebase")
@Provides
fun provideFirebaseTracker(): Tracker
```

---

## 🎨 Compose Multiplatform Integration

Designed for **Compose Multiplatform** from day one:

- App-level container created once
- Scoped containers per Screen / ViewModel
- Works with:
    - Android process recreation
    - iOS lifecycle boundaries
    - Desktop recomposition

No reflection. No magic. Only generated code.

---

## 🔄 Build Flow

1. Developer writes annotated code
2. KSP runs during compilation
3. Anchor-DI generates:
    - Factories
    - Containers
    - Scope holders
4. App uses generated code directly

**Runtime overhead is near zero.**

---

## 🚀 Benefits

- ⚡ Faster startup
- 🧠 Compile-time safety
- 🧩 Deterministic dependency graphs
- 📦 Minimal runtime footprint
- 🌍 True Kotlin Multiplatform support

---

## 🛠️ Project Status

🚧 **Early-stage / active development**

Planned milestones:
- Core annotation API
- KSP validation engine
- Multibinding implementation
- Compose lifecycle integration
- Maven Central publishing
- Documentation & samples

---

## 🧪 Who Should Use Anchor-DI?

- Kotlin Multiplatform SDK authors
- Compose Multiplatform applications
- Performance-sensitive apps
- Teams wanting predictable DI
- Android developers missing Hilt on iOS 😉

---

## 🤝 Contributing

Contributions, RFCs, and discussions are welcome.

This project aims to become a **foundational DI solution for Kotlin Multiplatform**.

---

## 📜 License

```
TBD
```
