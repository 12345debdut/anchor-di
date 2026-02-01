# Lazy and Provider

## Lazy&lt;T&gt;

Inject `Lazy<T>` when you want resolution to happen on **first access**, not at construction time.

```kotlin
class MyClass @Inject constructor(
    private val api: Lazy<Api>  // Resolved on first api.value access
) {
    fun doWork() {
        api.value.fetch()  // Resolution happens here
    }
}
```

Use when the dependency is expensive or not always needed.

## AnchorProvider&lt;T&gt;

Inject `AnchorProvider<T>` when you want a **new instance** every time you call `get()`.

```kotlin
class MyClass @Inject constructor(
    private val factory: AnchorProvider<UserRepository>
) {
    fun createRepository(): UserRepository = factory.get()  // New instance each time
}
```

Use when you need multiple instances or per-call creation.
