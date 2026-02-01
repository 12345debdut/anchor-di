# Navigation-Scoped DI

For **Compose Navigation** (Jetpack Navigation Compose on Android), you can scope objects to a **navigation destination** — one instance per destination, cleared when the destination is popped from the back stack. This is useful for screen-specific state, destination-level helpers, or destination analytics. This page explains how to define navigation-scoped bindings and use them in your NavHost.

---

## When to Use Navigation-Scoped DI

Use navigation-scoped bindings when:

- **Destination-level state** — State that should live as long as the user is on that screen (e.g. scroll position, form state) and be cleared when they navigate away.
- **Screen-specific helpers** — Objects used only by that destination (e.g. destination-specific analytics, screen-level cache).
- **Not ViewModel-scoped** — If the state or helper doesn't need to survive configuration changes (e.g. rotation), navigation-scoped can be simpler than ViewModel-scoped.

If you need state to survive configuration changes, use ViewModel-scoped bindings with `viewModelAnchor()` instead.

---

## Step 1: Define Navigation-Scoped Bindings

You can use `@NavigationScoped` on a class or install a module in `NavigationComponent`:

```kotlin
@NavigationScoped
class ScreenState @Inject constructor() {
    var count by mutableStateOf(0)
    var scrollPosition by mutableStateOf(0f)
}

@Module
@InstallIn(NavigationComponent::class)
object DestinationModule {
    @Provides
    fun provideDestinationHelper(): DestinationHelper = DestinationHelperImpl()
}
```

**What this does:** `ScreenState` and `DestinationHelper` are created once per navigation destination. When the user navigates away and the destination is popped, the scope is released and instances are eligible for GC.

---

## Step 2: Wrap Destination Content with NavigationScopedContent

On Android, wrap each NavHost destination with `NavigationScopedContent(navBackStackEntry)` and use `navigationScopedInject<T>()` inside:

```kotlin
NavHost(navController, startDestination = "home") {
    composable("home") {
        NavigationScopedContent(requireNotNull(it)) {
            val state = navigationScopedInject<ScreenState>()
            val helper = navigationScopedInject<DestinationHelper>()
            HomeScreen(state, helper)
        }
    }
    composable("detail") {
        NavigationScopedContent(requireNotNull(it)) {
            val state = navigationScopedInject<ScreenState>()
            DetailScreen(state)
        }
    }
}
```

**What this does:** `NavigationScopedContent(navBackStackEntry)` creates one `NavigationComponent` scope per `NavBackStackEntry`. The `it` in `composable("home") { ... }` is the `NavBackStackEntry` for that destination. `requireNotNull(it)` ensures it's non-null. Inside the content, `navigationScopedInject<T>()` resolves the type from that scope.

---

## Lifecycle

- **Created** — When the user navigates to the destination and the composable enters composition.
- **Cleared** — When the user navigates away and the destination is popped from the back stack.
- **Scope** — One scope per `NavBackStackEntry`. If the user navigates back to the same destination (creating a new entry), they get a new scope and new instances.

---

## ViewModel vs Navigation Scope

| Scope | Lifetime | Use Case |
|-------|----------|----------|
| **ViewModel** | Per ViewModel; survives configuration changes | Screen logic, use cases, state that should survive rotation |
| **Navigation** | Per destination; cleared when popped | Destination-level state, helpers, analytics that don't need to survive rotation |

You can use both in the same destination: `viewModelAnchor()` for ViewModels and `navigationScopedInject()` for navigation-scoped types.

---

## Dependencies

Add `androidx.navigation:navigation-compose` to the **androidMain** dependencies of the module that uses `NavigationScopedContent`. `anchor-di-compose` depends on it for Android.

---

## Next Steps

- **[Compose Overview](overview)** — `anchorInject()` and `viewModelAnchor()`
- **[Built-in Scopes](../scopes/built-in)** — Singleton, ViewModel, Navigation comparison
