# Dependency Injection in a Nutshell

## What is Dependency Injection?

**Dependency Injection (DI)** is a design pattern where objects receive their dependencies from the outside instead of creating them internally. This makes code:

- **Testable** — You can swap real implementations for mocks in tests
- **Flexible** — Change implementations without changing consumers
- **Decoupled** — Classes depend on abstractions, not concrete types

## Without DI (Tight Coupling)

```kotlin
class UserRepository {
    private val api = UserApiImpl()  // Hard-coded; hard to test
    // ...
}
```

## With DI (Loose Coupling)

```kotlin
class UserRepository @Inject constructor(
    private val api: UserApi  // Injected; easy to swap in tests
)
```

## Key Concepts

### 1. Binding

A **binding** tells the container how to provide an instance of a type. Anchor DI supports:

- **Constructor injection** — `@Inject` on the constructor; the container calls it with resolved dependencies
- **@Provides** — Manual factory in a module
- **@Binds** — Interface → implementation mapping

### 2. Scope

A **scope** controls how long an instance lives and how many instances exist:

- **Unscoped** — New instance every time
- **Singleton** — One instance for the app
- **ViewModel-scoped** — One instance per ViewModel
- **Navigation-scoped** — One instance per navigation destination
- **Custom** — One instance per your custom scope

### 3. Component

A **component** is an entry point into the dependency graph. Modules are installed in components; the component determines where bindings are available. Examples: `SingletonComponent`, `ViewModelComponent`, `NavigationComponent`.

## Why Compile-Time DI?

| Runtime DI (e.g. Koin) | Compile-Time DI (Anchor, Hilt) |
|------------------------|--------------------------------|
| Validates at runtime   | Validates at compile time      |
| Can fail in production | Fails the build if invalid     |
| Reflection or manual lookup | Generated code, no reflection |
| Slower startup         | Near-zero runtime overhead     |
