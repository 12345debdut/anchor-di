# Dependency Injection in a Nutshell

If you're new to Dependency Injection (DI), this page explains the core concepts in plain terms. You'll understand *why* DI matters and *how* Anchor DI implements it. No prior DI experience required.

---

## What is Dependency Injection?

**Dependency Injection** is a design pattern where objects receive their dependencies from the *outside* instead of creating them internally. In other words: instead of a class instantiating `UserApi` itself, something else (the "container" or "injector") provides `UserApi` and passes it in.

### Why does this matter?

| Benefit | Explanation |
|---------|-------------|
| **Testability** | In tests, you can inject a *fake* `UserApi` that returns mock data. You don't need a real network. |
| **Flexibility** | You can swap implementations (e.g. `UserApiImpl` vs `UserApiStagingImpl`) without changing the classes that use them. |
| **Decoupling** | Classes depend on abstractions (interfaces like `UserApi`) instead of concrete types. Easier to evolve and maintain. |

---

## Without DI: Tight Coupling

In this example, `UserRepository` creates `UserApiImpl` directly. It's tightly coupled to that implementation:

```kotlin
class UserRepository {
    private val api = UserApiImpl()  // Hard-coded dependency

    suspend fun loadUser(id: String): User = api.getUser(id)
}
```

**Problems:**

1. **Hard to test** — You can't replace `UserApiImpl` with a mock. Every test would hit the real API (or you'd need ugly hacks).
2. **Hard to change** — Want to use a different implementation (e.g. for staging)? You must change `UserRepository`.
3. **Hidden dependency** — `UserRepository` doesn't declare that it needs `UserApi`; you have to read the code to find out.

---

## With DI: Loose Coupling

In this example, `UserRepository` receives `UserApi` via its constructor. It doesn't know or care *which* implementation it gets:

```kotlin
class UserRepository @Inject constructor(
    private val api: UserApi  // Injected — provided by Anchor DI
) {
    suspend fun loadUser(id: String): User = api.getUser(id)
}
```

**Benefits:**

1. **Easy to test** — In tests, inject a fake: `UserRepository(FakeUserApi())`.
2. **Easy to change** — Swap implementations in a module; `UserRepository` stays the same.
3. **Explicit dependency** — The constructor clearly shows that `UserRepository` needs `UserApi`.

Anchor DI reads the `@Inject` constructor, finds the binding for `UserApi`, and passes it in when creating `UserRepository`.

---

## Key Concepts in Anchor DI

### 1. Binding

A **binding** tells the container how to provide an instance of a type. Anchor DI supports three ways to create bindings:

| Method | When to Use | Example |
|--------|-------------|---------|
| **Constructor injection** | Your own classes; you control the constructor. | `class UserRepository @Inject constructor(private val api: UserApi)` |
| **@Provides** | Third-party types, or when construction is more complex. | `@Provides fun provideHttpClient(): HttpClient = HttpClient { ... }` |
| **@Binds** | Interface → implementation mapping. The implementation must be injectable. | `@Binds fun bindApi(impl: ApiImpl): Api` |

The container uses bindings to satisfy constructor parameters and `@Provides` dependencies. Every type you inject must have a binding.

---

### 2. Scope

A **scope** controls *how long* an instance lives and *how many* instances exist:

| Scope | Lifetime | Example |
|-------|----------|---------|
| **Unscoped** | New instance every time you request it. | Stateless utilities, DTOs. |
| **Singleton** | One instance for the whole app. | HTTP client, database, config. |
| **ViewModel-scoped** | One instance per ViewModel. | Repository used only by one screen. |
| **Navigation-scoped** | One instance per navigation destination. | Screen-specific state or helpers. |
| **Custom** | One instance per your custom scope (e.g. Activity, session). | Navigator, session-specific analytics. |

Scoping prevents creating too many instances (e.g. a new HTTP client for every screen) and avoids memory leaks (e.g. a ViewModel holding a singleton that outlives the app).

---

### 3. Component

A **component** is an entry point into the dependency graph. You install modules in a component with `@InstallIn(Component::class)`. The component determines:

- **Where** bindings are available (e.g. only inside ViewModel scope)
- **How long** scoped instances live

| Component | Lifetime | Use Case |
|-----------|----------|----------|
| `SingletonComponent` | App lifetime | HTTP client, database, config |
| `ViewModelComponent` | Per ViewModel | Screen state, repository per screen |
| `NavigationComponent` | Per navigation destination | Destination-level helpers |
| Custom (e.g. `ActivityScope`) | Your lifecycle | Activity-scoped navigator, session scope |

---

## Why Compile-Time DI?

You might have used Koin or another runtime DI library. Here's how compile-time DI (Anchor, Hilt, Dagger) differs:

| Aspect | Runtime DI (e.g. Koin) | Compile-Time DI (Anchor, Hilt) |
|--------|------------------------|--------------------------------|
| **When validation happens** | At runtime, when you first resolve a type | At compile time, during the build |
| **If something is wrong** | App might crash in production ("No definition found") | Build fails — you fix it before shipping |
| **How it works** | Scans or registers definitions at startup; uses reflection or manual lookup | KSP generates factories and containers; no reflection |
| **Startup cost** | Can be slower (graph building, reflection) | Near zero — everything is pre-generated |

**Bottom line:** With Anchor DI, if your project compiles, your dependency graph is valid. You won't discover missing bindings or scope errors in production.

---

## Next Step

Ready to see it in code? Head to **[Quick Example](quick-example)** for a minimal setup you can run in minutes.
