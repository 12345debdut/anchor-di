# Real-World Example

This page walks you through building a **production-grade KMP app** with Anchor DI using a **Clean Architecture + MVVM** approach. You'll see how to structure layers, organize modules, and wire everything together. If you're new to KMP or DI, this example provides a concrete template you can follow.

---

## Architecture Overview

We'll follow a layered architecture:

```
┌─────────────────────────────────────────────────────────────────────────┐
│  UI (Compose) — commonMain                                               │
│  Screens, ViewModels (anchorInject, viewModelAnchor)                     │
│  Compose UI that observes ViewModels and displays state                  │
├─────────────────────────────────────────────────────────────────────────┤
│  Domain — commonMain                                                     │
│  Use cases, domain models                                                │
│  Business logic; no platform or framework dependencies                   │
├─────────────────────────────────────────────────────────────────────────┤
│  Data — commonMain                                                       │
│  Repositories (interfaces + implementations), API, data sources          │
│  Fetches and caches data; implements repository interfaces               │
├─────────────────────────────────────────────────────────────────────────┤
│  DI Modules — commonMain / platformMain                                  │
│  Network, Repositories, ViewModels                                       │
│  Wires dependencies across layers                                        │
└─────────────────────────────────────────────────────────────────────────┘
```

Dependencies flow **downward**: UI → Domain → Data. The UI doesn't know about data sources; the domain doesn't know about HTTP. Anchor DI wires everything together.

---

## Step 1: Project Setup

- **Shared module:** `commonMain` with `data`, `domain`, and `ui` packages (or separate modules).
- **KMP targets:** Android, iOS, Desktop, Web (as needed).
- **Add Anchor DI:** `anchor-di-api`, `anchor-di-core`, `anchor-di-compose`, `anchor-di-ksp` (see [Installation](../installation/setup)).

---

## Step 2: Data Layer

Define the API interface, repository interface, and implementations. The repository depends on the API; the API implementation depends on HTTP client (or platform-specific networking).

```kotlin
// commonMain — data/api/UserApi.kt
interface UserApi {
    suspend fun getUser(id: String): User
}

// commonMain — data/model/User.kt
data class User(val id: String, val name: String, val email: String)

// commonMain — data/repository/UserRepository.kt
interface UserRepository {
    suspend fun getUser(id: String): User
}

// commonMain — data/repository/UserRepositoryImpl.kt
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
) : UserRepository {
    override suspend fun getUser(id: String) = api.getUser(id)
}

// commonMain — di/DataModule.kt
@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    @Singleton
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
```

**What's happening:** The data layer exposes `UserRepository` as an interface. The implementation depends on `UserApi`. You'll need a module to provide `UserApi` (e.g. `@Binds` to `UserApiImpl` that uses `HttpClient`). The data module binds the implementation to the interface.

---

## Step 3: Domain Layer

Define use cases that depend on repositories. Use cases encapsulate business logic and are typically stateless.

```kotlin
// commonMain — domain/usecase/GetUserUseCase.kt
class GetUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String): User = repository.getUser(id)
}
```

**What's happening:** The use case depends on `UserRepository` (interface). Anchor DI resolves it from the data layer. The use case has no knowledge of API, HTTP, or platform.

---

## Step 4: ViewModel (ViewModel-Scoped)

ViewModels depend on use cases (or repositories, depending on your preference). We'll scope the use case to the ViewModel so each screen gets its own instance (or share a singleton use case if you prefer).

```kotlin
// commonMain — di/ViewModelModule.kt
@Module
@InstallIn(ViewModelComponent::class)
interface ViewModelModule {
    @Binds
    fun bindGetUserUseCase(impl: GetUserUseCase): GetUserUseCase
}

// commonMain — ui/user/UserViewModel.kt
@AnchorViewModel
class UserViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun loadUser(id: String) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            runCatching { getUserUseCase(id) }
                .onSuccess { _uiState.value = UserUiState.Success(it) }
                .onFailure { _uiState.value = UserUiState.Error(it.message) }
        }
    }
}
```

**What's happening:** The ViewModel depends on `GetUserUseCase`. The ViewModel module binds the use case in `ViewModelComponent`, so it's ViewModel-scoped. The ViewModel is created via `viewModelAnchor()` in Compose, so resolution runs inside the ViewModel scope.

---

## Step 5: UI (Compose)

Compose screens inject ViewModels with `viewModelAnchor()` and observe state.

```kotlin
// commonMain — ui/user/UserScreen.kt
@Composable
fun UserScreen(
    userId: String,
    viewModel: UserViewModel = viewModelAnchor()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }

    when (val state = uiState) {
        is UserUiState.Loading -> CircularProgressIndicator()
        is UserUiState.Success -> Text(state.user.name)
        is UserUiState.Error -> Text("Error: ${state.message}")
    }
}
```

**What's happening:** `viewModelAnchor()` creates the ViewModel inside the ViewModel scope, so its ViewModel-scoped dependencies (like `GetUserUseCase`) are resolved correctly.

---

## Step 6: Initialize

Call `Anchor.init(*getAnchorContributors())` once at app startup.

**Android:** In `Application.onCreate()` or before the first Composable.

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Anchor.init(*getAnchorContributors())
    }
}
```

**Compose (commonMain):** In the root Composable.

```kotlin
@Composable
fun App() {
    DisposableEffect(Unit) {
        Anchor.init(*getAnchorContributors())
        onDispose { }
    }
    AppContent()
}
```

---

## Summary

1. **Data layer** — Repositories, API, modules with `@Binds`/`@Provides` in `SingletonComponent`.
2. **Domain layer** — Use cases that depend on repositories; inject via constructor.
3. **ViewModel layer** — ViewModels that depend on use cases; scope use cases in `ViewModelComponent` if needed.
4. **UI layer** — Composables that inject ViewModels with `viewModelAnchor()`.
5. **Init** — Call `Anchor.init(*getAnchorContributors())` at startup.

Dependencies flow one way: UI → ViewModel → UseCase → Repository → Api. Anchor DI wires everything together. For more details, see [Installation](../installation/setup), [Core Concepts](../core/concepts), and [Compose Overview](../compose/overview).
