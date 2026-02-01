# Multi-binding

Anchor DI supports **Set** and **Map** multibindings (Dagger-style). Multiple modules can contribute to the same collection.

## Into Set

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

// Inject the full Set
class AnalyticsService @Inject constructor(
    private val trackers: Set<Tracker>
) {
    fun track(event: String) = trackers.forEach { it.track(event) }
}

// Or resolve manually
val trackers = Anchor.injectSet<Tracker>()
```

## Into Map

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

// Inject the full Map
class TrackerRegistry @Inject constructor(
    private val trackers: Map<String, Tracker>
)

// Or resolve manually
val trackers = Anchor.injectMap<Tracker>()
```

## Use Cases

- **Analytics:** Multiple trackers contributing to a `Set<Tracker>`
- **Plugins:** Map of plugin ID → implementation
- **Interceptors:** Set of HTTP interceptors
