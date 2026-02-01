# Qualifiers

Sometimes you need **multiple bindings for the same type**. For example, you might have two different base URLs (API and web), two different loggers (debug and release), or two different databases (main and cache). Anchor DI supports **qualifiers** to distinguish between them. This page explains how to use `@Named` and custom qualifiers.

---

## Why Qualifiers?

By default, the container has **one binding per type**. If you provide `String` twice (e.g. API base URL and web base URL), the container doesn't know which one to inject. Qualifiers let you label bindings so the container can choose the right one.

---

## @Named

The simplest qualifier is **@Named**. Use it on `@Provides` methods and on constructor parameters (or `@Inject` fields) to match them.

### Example

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @Provides
    @Named("apiBaseUrl")
    fun provideApiBaseUrl(): String = "https://api.example.com"

    @Provides
    @Named("webBaseUrl")
    fun provideWebBaseUrl(): String = "https://example.com"
}

class ApiClient @Inject constructor(
    @Named("apiBaseUrl") private val baseUrl: String
) {
    // baseUrl will be "https://api.example.com"
}
```

**What this does:** The `@Named("apiBaseUrl")` on the `@Provides` method and on the constructor parameter tells the container to inject the value from `provideApiBaseUrl()` when creating `ApiClient`.

---

## Custom Qualifier

For a cleaner API, you can define a **custom qualifier** annotation. Use it when you have a specific concept (e.g. API URL, web URL) and want a named annotation instead of a string.

### Step 1: Define the Qualifier

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebUrl
```

- **@Qualifier** — Marks this as a qualifier annotation (required by Anchor DI).
- **@Retention(BINARY)** — Keeps the annotation in the bytecode for runtime; you can use `SOURCE` if you only need compile-time.

### Step 2: Use It in Modules and Constructors

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @Provides
    @ApiUrl
    fun provideApiBaseUrl(): String = "https://api.example.com"

    @Provides
    @WebUrl
    fun provideWebBaseUrl(): String = "https://example.com"
}

class ApiClient @Inject constructor(
    @ApiUrl private val baseUrl: String
) {
    // baseUrl will be "https://api.example.com"
}
```

**Benefits of custom qualifiers:** More semantic (e.g. `@ApiUrl` vs `@Named("apiBaseUrl")`), easier to refactor, and less prone to typos.

---

## When to Use Qualifiers

| Scenario | Example |
|----------|---------|
| **Multiple instances of the same type** | Two `String` values (API URL, web URL), two `HttpClient` instances (main, auth) |
| **Configuration variants** | Debug vs release config, staging vs production base URL |
| **Multiple implementations of the same interface** | Primary vs secondary analytics, main vs cache database |

---

## Next Steps

- **[Modules and Bindings](modules-bindings)** — Organize bindings with modules
- **[Advanced: Multibinding](../advanced/multibinding)** — Contribute to `Set<T>` or `Map<String, V>` from multiple modules
