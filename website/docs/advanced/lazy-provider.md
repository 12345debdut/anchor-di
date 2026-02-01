# Lazy and Provider

Anchor DI supports **deferred resolution** and **per-call resolution** via `Lazy<T>` and `AnchorProvider<T>`. Use these when you want to control *when* or *how often* an instance is created. This page explains when and how to use them.

---

## Why Lazy and Provider?

By default, when you inject a dependency, the container creates or retrieves the instance at the point of injection (e.g. when the constructor is called). Sometimes you want:

1. **Deferred resolution** — Don't create the instance until first use (e.g. expensive dependencies that might not be needed).
2. **Per-call resolution** — Get a new instance every time you access it (e.g. factories, per-request creation).

Anchor DI provides two special types for this: `Lazy<T>` and `AnchorProvider<T>`.

---

## Lazy&lt;T&gt;

Inject `Lazy<T>` when you want resolution to happen on **first access**, not at construction time. The instance is created the first time you call `.value` (or `.get()`); subsequent calls return the cached instance.

### When to Use

- **Expensive dependencies** — Heavy objects that might not be needed (e.g. analytics, crash reporting).
- **Avoid circular dependencies** — Sometimes `Lazy` can help break cycles (though you should prefer restructuring the graph).
- **Startup optimization** — Delay creation of non-critical dependencies until after startup.

### Example

```kotlin
class MyClass @Inject constructor(
    private val analytics: Lazy<AnalyticsService>
) {
    fun onUserAction() {
        // Resolution happens here — on first access
        analytics.value.track("user_action")
    }

    fun doWork() {
        // Same instance — cached
        analytics.value.track("work_done")
    }
}
```

**How it works:** `Lazy<T>` is a Kotlin standard interface. Anchor DI provides a binding for `Lazy<T>` that wraps the binding for `T`. When you access `.value`, it resolves `T` (if not already resolved) and caches it for future access.

---

## AnchorProvider&lt;T&gt;

Inject `AnchorProvider<T>` when you want a **new instance** every time you call `get()`. Think of it as a factory: each call to `get()` creates or retrieves a new instance (depending on the binding's scope).

### When to Use

- **Multiple instances** — You need several instances of the same type (e.g. multiple `UserRepository` per user ID).
- **Per-request creation** — Each call should get a fresh instance (e.g. request-scoped objects).
- **Factory-like behavior** — You want to create instances on demand.

### Example

```kotlin
class MyClass @Inject constructor(
    private val repositoryFactory: AnchorProvider<UserRepository>
) {
    fun createRepository(): UserRepository = repositoryFactory.get()

    fun workWithMultipleRepos() {
        val repo1 = repositoryFactory.get()
        val repo2 = repositoryFactory.get()
        // repo1 and repo2 may be different instances (depending on scope)
    }
}
```

**How it works:** `AnchorProvider<T>` is an interface with a `get()` method. Anchor DI provides a binding that returns a provider for `T`. Each call to `get()` resolves `T` — if `T` is unscoped, you get a new instance; if it's scoped, you get the scoped instance.

---

## Comparison

| Type | Resolution | Use Case |
|------|------------|----------|
| **Direct injection** | At construction | Normal case — create instance when the dependent is created |
| **Lazy&lt;T&gt;** | On first access (`.value`) | Defer creation until first use; cache thereafter |
| **AnchorProvider&lt;T&gt;** | On each `get()` | Factory — create or retrieve instance on demand |

---

## Quick Reference

```kotlin
// Normal — resolved at construction
class A @Inject constructor(private val b: B)

// Lazy — resolved on first access
class A @Inject constructor(private val b: Lazy<B>)

// Provider — resolved on each get()
class A @Inject constructor(private val b: AnchorProvider<B>)
```

---

## Next Steps

- **[Multibinding](multibinding)** — Contribute to `Set<T>` or `Map<String, V>` from multiple modules
- **[Modules and Bindings](../core/modules-bindings)** — Organize bindings with modules
