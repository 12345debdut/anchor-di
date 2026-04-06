# Migrating from Koin

This guide helps you migrate a Kotlin Multiplatform project from Koin to Anchor DI. The biggest change: most errors that Koin discovers at runtime are caught at **build time** with Anchor DI.

---

## Side-by-Side Comparison

| Koin | Anchor DI | Notes |
|------|-----------|-------|
| `module { single { MyService() } }` | `@Singleton @Inject class MyService` | No runtime DSL needed |
| `get()` inside module | Constructor injection (automatic) | No service locator |
| `inject()` in Android | `Anchor.inject<T>()` at entry points | Only at roots |
| `viewModel { MyVM(get()) }` | `@AnchorViewModel @Inject class MyVM(dep: Dep)` | Auto-wired |
| `named("key")` | `@Named("key")` | Same concept |
| `scope(named("x")) { ... }` | `@Scoped("x")` | Validated at compile time |
| `startKoin { modules(...) }` | `Anchor.init()` | KSP discovers modules automatically |

---

## Pattern-by-Pattern Migration

### 1. Singleton

**Koin:**
```kotlin
val appModule = module {
    single { UserRepository(get()) }
}
```

**Anchor DI:**
```kotlin
@Singleton
@Inject
class UserRepository(private val apiService: ApiService)
```

### 2. Factory (Unscoped)

**Koin:**
```kotlin
val appModule = module {
    factory { DateFormatter() }
}
```

**Anchor DI:**
```kotlin
// No scope annotation = new instance each time
@Inject
class DateFormatter
```

### 3. Interface Binding

**Koin:**
```kotlin
val appModule = module {
    single<AuthService> { AuthServiceImpl(get()) }
}
```

**Anchor DI:**
```kotlin
@Module
@InstallIn(Component.Singleton::class)
interface AuthModule {
    @Binds
    fun bindAuthService(impl: AuthServiceImpl): AuthService
}

@Singleton
@Inject
class AuthServiceImpl(private val tokenStore: TokenStore) : AuthService
```

### 4. Third-Party Classes

**Koin:**
```kotlin
val networkModule = module {
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
```

**Anchor DI:**
```kotlin
@Module
@InstallIn(Component.Singleton::class)
class NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
}
```

### 5. Qualifiers

**Koin:**
```kotlin
val module = module {
    single(named("api")) { "https://api.example.com" }
    single(named("cdn")) { "https://cdn.example.com" }
}
```

**Anchor DI:**
```kotlin
@Module
@InstallIn(Component.Singleton::class)
class UrlModule {
    @Provides @Named("api")
    fun provideApiUrl(): String = "https://api.example.com"

    @Provides @Named("cdn")
    fun provideCdnUrl(): String = "https://cdn.example.com"
}

@Inject
class ApiClient(@Named("api") private val baseUrl: String)
```

### 6. ViewModel

**Koin:**
```kotlin
val module = module {
    viewModel { HomeViewModel(get(), get()) }
}

// In Compose
val viewModel: HomeViewModel = koinViewModel()
```

**Anchor DI:**
```kotlin
@AnchorViewModel
@Inject
class HomeViewModel(
    private val repo: UserRepository,
    private val analytics: AnalyticsService,
) : ViewModel()

// In Compose
val viewModel: HomeViewModel = viewModelAnchor()
```

### 7. Multibinding (Set)

**Koin:** No built-in support -- requires manual list assembly.

**Anchor DI:**
```kotlin
@Module
@InstallIn(Component.Singleton::class)
class AnalyticsModule {
    @Provides @IntoSet
    fun firebase(): AnalyticsTracker = FirebaseTracker()

    @Provides @IntoSet
    fun mixpanel(): AnalyticsTracker = MixpanelTracker()
}

// Automatically collected
@Inject
class AnalyticsService(private val trackers: Set<AnalyticsTracker>)
```

### 8. Initialization

**Koin:**
```kotlin
startKoin {
    modules(appModule, networkModule, viewModelModule)
}
```

**Anchor DI:**
```kotlin
// KSP discovers all @Module and @Inject classes at compile time
Anchor.init()
```

### 9. Testing

**Koin:**
```kotlin
class MyTest : KoinTest {
    @get:Rule val koinRule = KoinTestRule.create { modules(testModule) }
    @Test fun test() { val service: MyService = get() }
}
```

**Anchor DI:**
```kotlin
class MyTest {
    @BeforeTest fun setup() {
        Anchor.reset()
        Anchor.init(contributors = listOf(TestContributor()))
    }
    @Test fun test() { val service: MyService = Anchor.inject() }
}
```

---

## Compose Cheat Sheet

| Koin | Anchor DI |
|------|-----------|
| `koinInject<T>()` | `anchorInject<T>()` |
| `koinViewModel<T>()` | `viewModelAnchor<T>()` |
| `KoinApplication { }` | `Anchor.init()` |
| N/A | `navigationScopedInject<T>()` |

---

## Key Differences

1. **No `get()` calls in constructors.** Anchor DI wires dependencies via constructor injection. You never call a service locator inside a class body.

2. **Errors move to compile time.** Missing bindings, circular dependencies, and scope violations are caught by KSP during the build.

3. **Modules are optional.** Simple classes only need `@Inject`. You only create `@Module` classes for `@Provides` (third-party types) or `@Binds` (interface mappings).

4. **Multi-module projects are automatic.** Add the KSP processor (or convention plugin) to each Gradle module and set `anchorDiModuleId`. All contributors are discovered at initialization.

5. **~5x faster initialization.** See [Benchmarks](benchmarks) for details.
