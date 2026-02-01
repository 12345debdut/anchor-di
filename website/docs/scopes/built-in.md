---
sidebar_position: 1
---

# Built-in Scopes

Anchor DI provides three built-in scopes: **SingletonComponent**, **ViewModelComponent**, and **NavigationComponent**. This page explains when to use each one, how they differ, and how to use them in practice. Understanding scopes is key to designing a clean and memory-efficient dependency graph.

---

## Why Scopes Matter

Without scopes, every time you request a type you might get a new instance. That's fine for stateless objects, but for things like an HTTP client or a database connection, you typically want **one instance** shared across the app. For screen-specific state (e.g. a repository used only by one screen), you want **one instance per screen** — not one for the whole app (too broad) and not a new one every time you inject (unnecessary overhead and potential leaks).

Scopes let you control:

- **Lifetime** — How long an instance lives (app, screen, destination)
- **Cardinality** — How many instances exist (one for app, one per ViewModel, etc.)

---

## SingletonComponent

**Lifetime:** Application-wide — one instance for the whole app.

**Use for:** HTTP client, database, config, platform context, logging, analytics — anything that should live as long as the app and be shared everywhere.

### Example

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
}
```

**What this does:**

- `@InstallIn(SingletonComponent::class)` — This module is part of the app-wide (singleton) component.
- `@Provides` — Manually construct and return an instance.
- `@Singleton` — Cache the instance; return the same one for every request.

**How to resolve:** Use `Anchor.inject<HttpClient>()` anywhere, or inject `HttpClient` into any other type (Repository, ViewModel, etc.). The same instance will be used everywhere.

**Tip:** Any type requested from the root container or from a child scope can access singletons — they're always visible.

---

## ViewModelComponent

**Lifetime:** One instance per ViewModel — when the ViewModel is cleared, scoped instances are eligible for GC.

**Use for:** Screen state, repository used only by one screen, use cases that are screen-specific. When you want an instance to live as long as a single screen (ViewModel) and be shared across that screen's logic, but not leak into other screens or the app.

### Example

```kotlin
@Module
@InstallIn(ViewModelComponent::class)
interface ViewModelModule {
    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}

@AnchorViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    // UserRepository is created once per ViewModel; shared for this screen
    fun loadUser() { /* ... */ }
}
```

**What this does:**

- `@InstallIn(ViewModelComponent::class)` — Types provided in this module are ViewModel-scoped.
- `@Binds` — Map interface `UserRepository` to implementation `UserRepositoryImpl`.
- `@AnchorViewModel` — Mark this class as an Anchor ViewModel; it will be created via `viewModelAnchor()`.
- `viewModelAnchor()` — Runs resolution inside `ViewModelComponent` scope so the ViewModel and its ViewModel-scoped dependencies (like `UserRepository`) are created in the correct scope.

**Important:** Resolve ViewModel-scoped types **only** inside the ViewModel scope. Use `viewModelAnchor()` in Compose — **not** `viewModel { Anchor.inject<ViewModel>() }`. If you use `viewModel { Anchor.inject<MyViewModel>() }`, resolution runs at the root, and ViewModel-scoped dependencies (like a repository from `@InstallIn(ViewModelComponent::class)`) will fail with "Scoped binding for X requires a scope."

---

## NavigationComponent

**Lifetime:** One instance per navigation destination — cleared when the destination is popped from the back stack.

**Use for:** Destination-level state (e.g. scroll position, form state), screen-specific helpers, destination analytics. When you want an instance to live as long as the user is on that screen (navigation destination) and be cleared when they navigate away.

### Example

```kotlin
@NavigationScoped
class ScreenState @Inject constructor() {
    var scrollPosition by mutableStateOf(0)
}

@Module
@InstallIn(NavigationComponent::class)
object DestinationModule {
    @Provides
    fun provideDestinationHelper(): DestinationHelper = DestinationHelperImpl()
}

// In your NavHost (Android)
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

**What this does:**

- `@NavigationScoped` — This class is created once per navigation destination.
- `@InstallIn(NavigationComponent::class)` — Types provided in this module are navigation-scoped.
- `NavigationScopedContent(navBackStackEntry)` — Creates one `NavigationComponent` scope per `NavBackStackEntry`. When the user navigates away and the destination is popped, the scope is released.
- `navigationScopedInject<T>()` — Resolves the type from the current navigation scope.

**Requires:** `anchor-di-compose` and `androidx.navigation:navigation-compose` (for Android).

---

## Comparison

| Scope | Lifetime | Where to Resolve | Typical Use |
|-------|----------|------------------|-------------|
| **Singleton** | App | `Anchor.inject<T>()` or inject into any type | HTTP client, database, config, logging |
| **ViewModel** | Per ViewModel | Inside ViewModel created with `viewModelAnchor()` | Screen state, repository per screen |
| **Navigation** | Per destination | Inside `NavigationScopedContent` with `navigationScopedInject()` | Destination-level state, destination helpers |

---

## Choosing the Right Scope

Ask yourself:

1. **Does it need to live for the whole app?** → Singleton
2. **Does it need to live only while the user is on a screen?** → ViewModel
3. **Does it need to live only while the user is on a navigation destination (and be cleared when they leave)?** → Navigation

When in doubt, start with the narrowest scope that works — it's easier to widen scope later than to fix leaks from overly broad scopes.

---

## Next Steps

- **[Creating Custom Scopes](custom-scopes)** — Define scopes that match your lifecycle (e.g. Activity, session)
- **[Compose Integration](../compose/overview)** — `anchorInject()` and `viewModelAnchor()` in Composables
