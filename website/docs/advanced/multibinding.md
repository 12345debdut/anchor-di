# Multi-binding

Anchor DI supports **Set** and **Map** multibindings (Dagger-style). When you need to collect contributions from **multiple modules** into a single `Set<T>` or `Map<String, V>`, multibinding is the right tool. This page explains how to use `@IntoSet` and `@IntoMap`.

---

## Why Multibinding?

Sometimes you want **multiple implementations** of the same interface, collected into a single structure. For example:

- **Analytics** — Multiple trackers (Firebase, Amplitude, Crashlytics) contribute to a `Set<Tracker>`.
- **Plugins** — Multiple plugins contribute to a `Map<String, Plugin>`.
- **Interceptors** — Multiple HTTP interceptors contribute to a `Set<Interceptor>`.

Without multibinding, you'd have to manually collect implementations in a module. With multibinding, each module contributes independently, and Anchor DI combines them.

---

## Into Set

Use `@IntoSet` when you want to contribute one element to a multibound `Set<T>`. Multiple modules can contribute; all contributions are combined into a single set.

### Example

```kotlin
interface Tracker {
    fun track(event: String)
}

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {
    @IntoSet
    @Provides
    fun provideFirebaseTracker(): Tracker = FirebaseTracker()

    @IntoSet
    @Provides
    fun provideAmplitudeTracker(): Tracker = AmplitudeTracker()
}

class AnalyticsService @Inject constructor(
    private val trackers: Set<Tracker>
) {
    fun track(event: String) {
        trackers.forEach { it.track(event) }
    }
}
```

**What this does:** When `AnalyticsService` is created, it receives a `Set<Tracker>` containing both `FirebaseTracker` and `AmplitudeTracker`. Each module contributes one element; the container combines them.

**Manual resolution:** You can also resolve the set directly: `val trackers = Anchor.injectSet<Tracker>()`.

---

## Into Map

Use `@IntoMap` when you want to contribute one entry to a multibound `Map<String, V>`. Use `@StringKey` to specify the key. Multiple modules can contribute; all contributions are combined into a single map.

### Example

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object TrackerModule {
    @IntoMap
    @StringKey("firebase")
    @Provides
    fun provideFirebaseTracker(): Tracker = FirebaseTracker()

    @IntoMap
    @StringKey("amplitude")
    @Provides
    fun provideAmplitudeTracker(): Tracker = AmplitudeTracker()
}

class TrackerRegistry @Inject constructor(
    private val trackers: Map<String, Tracker>
) {
    fun getTracker(name: String): Tracker? = trackers[name]
}
```

**What this does:** When `TrackerRegistry` is created, it receives a `Map<String, Tracker>` with keys `"firebase"` and `"amplitude"`. Each module contributes one entry; the container combines them.

**Manual resolution:** You can also resolve the map directly: `val trackers = Anchor.injectMap<Tracker>()`.

---

## Use Cases

| Use Case | Structure | Example |
|----------|-----------|---------|
| **Analytics** | `Set<Tracker>` | Firebase, Amplitude, Crashlytics — all receive events |
| **Plugins** | `Map<String, Plugin>` | Plugin ID → implementation |
| **Interceptors** | `Set<Interceptor>` | Auth, logging, retry — all run on requests |

---

## Rules

- **Set keys:** No duplicate types in a single set (each contribution is one element).
- **Map keys:** No duplicate keys — KSP validates at compile time; duplicate keys fail the build.
- **Scope:** Multibound sets and maps are typically singleton (provided in `SingletonComponent`).

---

## Quick Reference

```kotlin
// Contribute to Set
@IntoSet
@Provides
fun provideTracker(): Tracker = MyTracker()

// Contribute to Map
@IntoMap
@StringKey("key")
@Provides
fun provideTracker(): Tracker = MyTracker()

// Inject
class Service @Inject constructor(
    private val trackers: Set<Tracker>
)

class Registry @Inject constructor(
    private val trackers: Map<String, Tracker>
)

// Or resolve manually
val trackers = Anchor.injectSet<Tracker>()
val map = Anchor.injectMap<Tracker>()
```

---

## Next Steps

- **[Qualifiers](../core/qualifiers)** — Disambiguate multiple bindings for the same type
- **[Modules and Bindings](../core/modules-bindings)** — Organize bindings with modules
