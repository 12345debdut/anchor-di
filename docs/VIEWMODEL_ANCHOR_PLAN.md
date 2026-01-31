# Plan for Using viewModelAnchor / navViewModelAnchor

This doc summarizes **when and how** to use `viewModelAnchor()` vs `navViewModelAnchor()` with `@AnchorViewModel` ViewModels.

---

## Two ways to get a ViewModel

| API | Scope | When to use |
|-----|--------|-------------|
| **viewModelAnchor()** | Tied to a **ViewModelStoreOwner** (Activity, Fragment, or NavBackStackEntry from Jetpack NavHost). | Use when your Compose tree is under a platform-provided ViewModel store (e.g. Android Activity or Jetpack Navigation **NavHost** where each destination has a `NavBackStackEntry`). |
| **navViewModelAnchor()** | Tied to the current **navigation entry** inside `NavigationScopedContent(scopeKey)`. | Use when you use **Navigation 3** (NavDisplay, user-owned back stack) or any custom nav where you wrap content in `NavigationScopedContent(scopeKey)`. No platform ViewModelStoreOwner needed. |

Both resolve **ViewModelComponent**-scoped types (including `@AnchorViewModel`). No need to register ViewModels in NavigationComponent when using `navViewModelAnchor()`.

---

## Flow

1. **Mark the ViewModel**  
   Annotate the class with `@AnchorViewModel` and use an `@Inject` constructor. KSP will bind it to **ViewModelComponent**.

2. **Choose the scope**
   - **Platform ViewModel store (Activity / Jetpack NavHost):**  
     Use **viewModelAnchor()** in your composable. The ViewModel is created once per `ViewModelStoreOwner` (e.g. per NavBackStackEntry) and cleared when that owner is cleared.
   - **Navigation 3 / custom nav:**  
     Wrap destination content in **NavigationScopedContent(scopeKey)** and use **navViewModelAnchor()** inside. The ViewModel is created once per `scopeKey` and cleared when that entry leaves composition (e.g. destination popped).

3. **Inject in Compose**
   - `viewModelAnchor<T>()` — requires a ViewModelStoreOwner in composition (e.g. under Activity or Jetpack NavHost).
   - `navViewModelAnchor<T>()` — requires being inside `NavigationScopedContent(scopeKey) { ... }`.

---

## Example: viewModelAnchor (Jetpack NavHost)

```kotlin
NavHost(navController, startDestination = "home") {
    composable("home") { backStackEntry ->
        // BackStackEntry is the ViewModelStoreOwner
        HomeScreen()  // inside: viewModelAnchor<HomeViewModel>()
    }
}
```

---

## Example: navViewModelAnchor (Navigation 3)

```kotlin
NavDisplay(backStack = backStack, ...) {
    entryProvider = entryProvider {
        entry<ProductListRoute> {
            NavigationScopedContent(ProductListRoute) {
                ProductListScreen()  // inside: navViewModelAnchor<ProductListViewModel>()
            }
        }
        entry<ProductDetailsRoute> { key ->
            NavigationScopedContent(key.id) {
                ProductDetailsScreen(...)  // inside: navViewModelAnchor<ProductDetailsViewModel>()
            }
        }
    }
}
```

---

## Summary

- **@AnchorViewModel** = ViewModel is bound to **ViewModelComponent** (KSP).
- **viewModelAnchor()** = get that ViewModel from the **current ViewModelStoreOwner** (platform).
- **navViewModelAnchor()** = get that ViewModel from the **current navigation scope** (NavigationScopedContent).
- Use **viewModelAnchor** when you have a ViewModelStoreOwner (Activity / Jetpack NavHost).  
- Use **navViewModelAnchor** when you use Navigation 3 or custom nav with `NavigationScopedContent`.
