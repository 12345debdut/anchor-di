# Real-World Example

A step-by-step guide to building a production-grade KMP app with Anchor DI using a Clean Architecture + MVVM approach.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│  UI (Compose) - commonMain                              │
│  Screens, ViewModels (anchorInject, viewModelAnchor)    │
├─────────────────────────────────────────────────────────┤
│  Domain - commonMain                                    │
│  Use cases, domain models                               │
├─────────────────────────────────────────────────────────┤
│  Data - commonMain                                      │
│  Repositories, API, data sources                        │
├─────────────────────────────────────────────────────────┤
│  DI Modules - commonMain / platformMain                 │
│  Network, Repositories, ViewModels                      │
└─────────────────────────────────────────────────────────┘
```

## 1. Project Setup

- Shared module: `commonMain` with data, domain, UI layers
- KMP targets: Android, iOS, Desktop, Web
- Add Anchor DI dependencies (api, core, compose, ksp)

## 2. Data Layer

```kotlin
// Api interface
interface UserApi {
    suspend fun getUser(id: String): User
}

// Repository
interface UserRepository {
    suspend fun getUser(id: String): User
}

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
) : UserRepository {
    override suspend fun getUser(id: String) = api.getUser(id)
}

// Module
@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    @Singleton
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
```

## 3. Domain Layer

```kotlin
class GetUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String) = repository.getUser(id)
}
```

## 4. ViewModel (ViewModel-scoped)

```kotlin
@Module
@InstallIn(ViewModelComponent::class)
interface ViewModelModule {
    @Binds
    fun bindGetUserUseCase(impl: GetUserUseCase): GetUserUseCase
}

@AnchorViewModel
class UserViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {
    // ...
}
```

## 5. UI (Compose)

```kotlin
@Composable
fun UserScreen(
    viewModel: UserViewModel = viewModelAnchor()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

## 6. Initialize

```kotlin
// Android: Application.onCreate()
Anchor.init(*getAnchorContributors())

// Or in App composable (commonMain)
DisposableEffect(Unit) {
    Anchor.init(*getAnchorContributors())
    onDispose { }
}
```

This structure keeps dependencies flowing one way (UI → ViewModel → UseCase → Repository → Api) and uses Anchor DI to wire everything together.
