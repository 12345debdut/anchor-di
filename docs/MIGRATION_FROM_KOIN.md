# Migrating from Koin to Anchor DI

This guide helps you migrate a Kotlin Multiplatform project from Koin to Anchor DI. Anchor DI is a compile-time DI framework — most errors that Koin reports at runtime are caught at build time with Anchor DI.

---

## Quick Comparison

| Feature | Koin | Anchor DI |
|---------|------|-----------|
| Binding declaration | `module { single { ... } }` (runtime DSL) | `@Singleton @Inject class ...` (annotations) |
| Dependency resolution | `get()` / `inject()` (runtime lookup) | Constructor injection (compile-time wired) |
| Error detection | Runtime (`NoBeanDefFoundException`) | Compile-time (KSP validation) |
| Multiplatform | Yes (Koin 4.x) | Yes (KMP from day one) |
| Code generation | None | KSP generates factories + wiring |

---

## Step-by-Step Migration

### 1. Singleton Binding

**Koin:**
```kotlin
// Module declaration
val appModule = module {
    single { UserRepository(get()) }
}

// Usage
class MyViewModel : ViewModel() {
    private val repo: UserRepository by inject()
}
```

**Anchor DI:**
```kotlin
// Just annotate the class — no module DSL needed
@Singleton
@Inject
class UserRepository(private val apiService: ApiService)

// Usage — constructor injection is automatic
@Inject
class MyViewModel(private val repo: UserRepository) : ViewModel()
```

> Anchor DI resolves the full dependency chain at compile time. If `ApiService` is missing, you get a build error — not a runtime crash.

---

### 2. Factory (Unscoped) Binding

**Koin:**
```kotlin
val appModule = module {
    factory { DateFormatter() }
}
```

**Anchor DI:**
```kotlin
// No scope annotation = new instance each time (factory behavior)
@Inject
class DateFormatter
```

---

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

---

### 4. Providing Third-Party Classes

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

---

### 5. Named / Qualified Bindings

**Koin:**
```kotlin
val module = module {
    single(named("api")) { "https://api.example.com" }
    single(named("cdn")) { "https://cdn.example.com" }
}

// Usage
class ApiClient(
    @InjectedParam val baseUrl: String // manual wiring
)
```

**Anchor DI:**
```kotlin
@Module
@InstallIn(Component.Singleton::class)
class UrlModule {
    @Provides
    @Named("api")
    fun provideApiUrl(): String = "https://api.example.com"

    @Provides
    @Named("cdn")
    fun provideCdnUrl(): String = "https://cdn.example.com"
}

// Usage — qualifier on constructor parameter
@Inject
class ApiClient(@Named("api") private val baseUrl: String)
```

---

### 6. ViewModel Scoping

**Koin:**
```kotlin
val viewModelModule = module {
    viewModel { HomeViewModel(get(), get()) }
}

// In Compose
@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = koinViewModel()
}
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
@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = viewModelAnchor()
}
```

---

### 7. Custom Scopes

**Koin:**
```kotlin
val module = module {
    scope(named("session")) {
        scoped { SessionManager(get()) }
    }
}

// Create/get scope manually
val sessionScope = getKoin().createScope("sessionId", named("session"))
val manager = sessionScope.get<SessionManager>()
// Must remember to close
sessionScope.close()
```

**Anchor DI:**
```kotlin
@Scoped("session")
@Inject
class SessionManager(private val tokenStore: TokenStore)

// Scope lifecycle is managed declaratively
// In Compose with navigation scoping:
@Composable
fun SessionScreen() {
    NavigationScopedContent(scopeKey = "session") {
        val manager: SessionManager = navigationScopedInject()
    }
}
```

---

### 8. Multibinding (Set)

**Koin:**
```kotlin
// Koin doesn't have built-in multibinding — manual list assembly
val module = module {
    single { FirebaseTracker() as AnalyticsTracker }
    single { MixpanelTracker() as AnalyticsTracker }
    single {
        AnalyticsService(listOf(get<FirebaseTracker>(), get<MixpanelTracker>()))
    }
}
```

**Anchor DI:**
```kotlin
@Module
@InstallIn(Component.Singleton::class)
class AnalyticsModule {
    @Provides
    @IntoSet
    fun firebaseTracker(): AnalyticsTracker = FirebaseTracker()

    @Provides
    @IntoSet
    fun mixpanelTracker(): AnalyticsTracker = MixpanelTracker()
}

// Automatically collected — inject the Set
@Inject
class AnalyticsService(private val trackers: Set<AnalyticsTracker>)
```

---

### 9. Multibinding (Map)

**Koin:**
```kotlin
// No built-in map multibinding
```

**Anchor DI:**
```kotlin
@Module
@InstallIn(Component.Singleton::class)
class FeatureFlagModule {
    @Provides
    @IntoMap
    @StringKey("dark_mode")
    fun darkModeFlag(): FeatureFlag = DarkModeFlag()

    @Provides
    @IntoMap
    @StringKey("new_onboarding")
    fun onboardingFlag(): FeatureFlag = OnboardingFlag()
}

@Inject
class FeatureFlagManager(private val flags: Map<String, FeatureFlag>)
```

---

### 10. Initialization

**Koin:**
```kotlin
// Application.onCreate or common entry point
startKoin {
    modules(appModule, networkModule, viewModelModule)
}
```

**Anchor DI:**
```kotlin
// Application.onCreate or common entry point
Anchor.init()

// That's it. KSP discovers all @Module and @Inject classes at compile time.
// No manual module listing required.
```

For multi-module projects, each Gradle module generates a contributor automatically via the `anchorDiModuleId` KSP argument.

---

### 11. Testing

**Koin:**
```kotlin
class MyTest : KoinTest {
    @get:Rule
    val koinRule = KoinTestRule.create {
        modules(testModule)
    }

    @Test
    fun test() {
        val service: MyService = get()
    }
}
```

**Anchor DI:**
```kotlin
class MyTest {
    @BeforeTest
    fun setup() {
        Anchor.reset()
        Anchor.init(
            contributors = listOf(TestContributor())
        )
    }

    @Test
    fun test() {
        val service: MyService = Anchor.inject()
    }
}
```

---

## Compose Integration Cheat Sheet

| Koin | Anchor DI | Notes |
|------|-----------|-------|
| `koinInject<T>()` | `anchorInject<T>()` | Simple injection in Composable |
| `koinViewModel<T>()` | `viewModelAnchor<T>()` | ViewModel with scope management |
| `KoinApplication { }` | `Anchor.init()` | One-time initialization |
| N/A | `navigationScopedInject<T>()` | Navigation-scoped injection (unique to Anchor DI) |

---

## Key Differences to Keep in Mind

1. **No `get()` calls in constructors.** Anchor DI wires dependencies via constructor injection. You never call a service locator inside a class body.

2. **Errors move to compile time.** Missing bindings, circular dependencies, and scope violations are all caught by KSP during the build. This is the biggest win over Koin.

3. **Modules are optional.** Simple classes only need `@Inject`. You only create `@Module` classes when you need `@Provides` (third-party types) or `@Binds` (interface mappings).

4. **Multi-module projects are automatic.** Add the KSP processor to each Gradle module and set `anchorDiModuleId`. All contributors are discovered at initialization — no manual module aggregation.

5. **Scope lifecycle is declarative.** Navigation and ViewModel scopes are managed through annotations and Compose integration, not manual `createScope()`/`close()` calls.
