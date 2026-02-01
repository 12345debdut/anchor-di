# Navigation-Scoped DI

For **Compose Navigation** (Jetpack Navigation Compose on Android), you can scope objects to a **navigation destination** — one instance per destination, cleared when the destination is popped.

## Define Navigation-Scoped Bindings

```kotlin
@NavigationScoped
class ScreenState @Inject constructor() {
    var count by mutableStateOf(0)
}

@Module
@InstallIn(NavigationComponent::class)
object DestinationModule {
    @Provides
    fun provideDestinationHelper(): DestinationHelper = DestinationHelperImpl()
}
```

## Wrap Destination Content

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

## Lifecycle

- `NavigationScopedContent(navBackStackEntry)` creates one `NavigationComponent` scope per `NavBackStackEntry`.
- When the user navigates away and the destination is popped, the scope is released.
- Use navigation-scoped for state or services that should live as long as the destination (e.g. screen-level cache, destination-specific analytics).
- For ViewModel-scoped dependencies, keep using `viewModelAnchor()` inside the same destination.

## Dependencies

Add `androidx.navigation:navigation-compose` to the **androidMain** dependencies of the module that uses `NavigationScopedContent`. `anchor-di-compose` depends on it for Android.
