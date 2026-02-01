---
sidebar_position: 1
---

# Built-in Scopes

## SingletonComponent

**Lifetime:** Application-wide (one instance for the whole app)

**Use for:** HTTP client, database, config, platform context, logging, analytics

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient { ... }
}
```

Resolve with `Anchor.inject<HttpClient>()` or inject into other types.

## ViewModelComponent

**Lifetime:** One instance per ViewModel

**Use for:** Screen state, repository per screen, use cases used only by one screen

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
) : ViewModel()
```

**Important:** Resolve ViewModel-scoped types only inside ViewModel scope. Use `viewModelAnchor()` in Compose — not `viewModel { Anchor.inject<ViewModel>() }`.

## NavigationComponent

**Lifetime:** One instance per navigation destination (cleared when destination is popped)

**Use for:** Destination-level state, screen-specific helpers, destination analytics

```kotlin
@NavigationScoped
class ScreenState @Inject constructor() { ... }

NavHost(navController, startDestination = "home") {
    composable("home") {
        NavigationScopedContent(requireNotNull(it)) {
            val state = navigationScopedInject<ScreenState>()
            HomeScreen(state)
        }
    }
}
```

Requires `anchor-di-compose` and Android Navigation Compose.

## Comparison

| Scope | Lifetime | Where to Resolve |
|-------|----------|------------------|
| Singleton | App | Anywhere (`Anchor.inject<T>()`) |
| ViewModel | Per ViewModel | Inside ViewModel scope (`viewModelAnchor()`) |
| Navigation | Per destination | Inside `NavigationScopedContent` (`navigationScopedInject()`) |
