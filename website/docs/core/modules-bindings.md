# Modules and Bindings

This page explains how to organize and define bindings in Anchor DI. You'll learn when to use `@Module`, `@Provides`, `@Binds`, and `@Inject` constructors, and how they work together. If you're new to DI or coming from Hilt/Dagger, these concepts will feel familiar.

---

## What is a Module?

A **module** groups bindings. Think of it as a "recipe book" that tells Anchor DI how to provide certain types. You use `@Module` on an `object` or `abstract class`, and `@InstallIn(Component::class)` to declare where it's installed (Singleton, ViewModel, Navigation, or a custom component).

Modules are useful when:

- You need to provide **third-party types** (e.g. `HttpClient`, `OkHttpClient`) that you can't annotate with `@Inject`
- You need **manual construction logic** (e.g. complex configuration)
- You need to **bind interfaces to implementations** (e.g. `UserApi` → `UserApiImpl`)

---

## @Module and @InstallIn

Every module must be annotated with `@Module` and `@InstallIn(Component::class)`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // bindings go here
}
```

- **@Module** — Marks this class as a module.
- **@InstallIn(SingletonComponent::class)** — Installs this module in `SingletonComponent`, so its bindings are available in the app-wide (singleton) scope. You can use `ViewModelComponent`, `NavigationComponent`, or a custom component.

---

## @Provides

Use **@Provides** when you need to manually construct an instance. The method must return the type you're providing.

### When to Use

- **Third-party types** — You can't add `@Inject` to their constructors (e.g. `HttpClient`, `Retrofit`)
- **Complex construction** — Configuration, builders, or conditional logic
- **Platform-specific types** — Use `expect`/`actual` or platform modules

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

**What this does:** When something requests `HttpClient`, the container calls `provideHttpClient()` and returns the result. The `@Singleton` scope means it's cached — the same instance is returned for every request.

**Dependencies in @Provides:** If `provideHttpClient()` needs other types (e.g. a config), add them as parameters — the container will resolve them:

```kotlin
@Provides
@Singleton
fun provideHttpClient(config: AppConfig): HttpClient = HttpClient {
    baseUrl = config.apiBaseUrl
    // ...
}
```

---

## @Binds

Use **@Binds** for interface → implementation mapping. The method must be `abstract`, take exactly one parameter (the implementation), and return the interface type. The implementation must be injectable (have an `@Inject` constructor or be provided elsewhere).

### When to Use

- **Interface → implementation** — You want to depend on an interface (e.g. `UserApi`) but provide a concrete implementation (e.g. `UserApiImpl`)
- **Cleaner than @Provides** — No manual instantiation; Anchor DI creates the implementation and returns it as the interface type

### Example

```kotlin
@Module
@InstallIn(SingletonComponent::class)
interface ApiModule {
    @Binds
    @Singleton
    fun bindApi(impl: ApiImpl): Api
}

class ApiImpl @Inject constructor(
    private val httpClient: HttpClient
) : Api {
    // ...
}
```

**What this does:** When something requests `Api`, the container creates `ApiImpl` (resolving its dependencies like `HttpClient`), and returns it as `Api`. The `@Binds` method doesn't need a body — Anchor DI generates the implementation.

**Why use an interface?** Interfaces make code testable (you can inject a mock in tests) and flexible (you can swap implementations without changing consumers).

---

## @Inject Constructor

For classes you control, **constructor injection** is the simplest and most common approach. Add `@Inject` to the constructor; no module is needed for the class itself.

### When to Use

- **Your own classes** — Repositories, use cases, ViewModels, etc.
- **Simple dependencies** — The constructor parameters are types the container can resolve

### Example

```kotlin
@Singleton
class UserRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun loadUser(id: String) = api.getUser(id)
}
```

**What this does:** When something requests `UserRepository`, the container resolves `UserApi` (from a binding in a module or another `@Inject` class), creates `UserRepository` with that `UserApi`, and returns it.

**No module needed** — The container discovers `@Inject` constructors via KSP. You only need a module if you're providing `UserApi` (e.g. with `@Binds` or `@Provides`).

---

## Combining Approaches

You can mix `@Inject`, `@Provides`, and `@Binds` in the same project. The container builds a dependency graph; as long as every type has a binding (or is constructible via `@Inject`), resolution succeeds.

Example flow:

1. `UserViewModel` has `@Inject constructor(private val userRepository: UserRepository)`.
2. `UserRepository` has `@Inject constructor(private val api: UserApi)`.
3. `UserApi` is an interface; a module has `@Binds fun bindApi(impl: UserApiImpl): UserApi`.
4. `UserApiImpl` has `@Inject constructor(private val httpClient: HttpClient)`.
5. A module has `@Provides fun provideHttpClient(): HttpClient`.

The container resolves the chain: `HttpClient` → `UserApiImpl` → `UserApi` → `UserRepository` → `UserViewModel`.

---

## Quick Reference

| Approach | When to Use | Example |
|----------|-------------|---------|
| **@Inject constructor** | Your own classes | `class UserRepository @Inject constructor(private val api: UserApi)` |
| **@Provides** | Third-party types, complex construction | `@Provides fun provideHttpClient(): HttpClient = HttpClient { ... }` |
| **@Binds** | Interface → implementation | `@Binds fun bindApi(impl: ApiImpl): Api` |

---

## Next Steps

- **[Qualifiers](qualifiers)** — Disambiguate multiple bindings for the same type
- **[Built-in Scopes](../scopes/built-in)** — When to use Singleton vs ViewModel vs Navigation scope
