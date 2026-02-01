# expect / actual and Platform Modules

Anchor DI works with **expect / actual** for platform-specific dependencies. When you need types that exist only on one platform (e.g. `Context` on Android, `UIApplication` on iOS), you define an `expect` type in `commonMain` and provide `actual` implementations in each platform source set. This page explains the pattern and how to use it with Anchor DI.

---

## Why expect / actual?

In KMP, shared code in `commonMain` cannot reference platform-specific types like `Context` or `UIApplication` directly — they don't exist on all platforms. **expect / actual** lets you declare an abstract type in `commonMain` and provide platform-specific implementations in `androidMain`, `iosMain`, etc. Shared code depends on the abstract type; each platform provides its own implementation.

---

## The Pattern

1. **Define** an `expect` type in `commonMain`.
2. **Implement** the `actual` type in each platform source set.
3. **Provide** it via a module in a platform-specific source set (or in `commonMain` if the construction is abstracted).

---

## Example: Platform Logger

Suppose you want a logger that uses `Log.d` on Android and `NSLog` on iOS. You'd do:

### Step 1: Define expect in commonMain

```kotlin
// commonMain
expect class PlatformLogger {
    fun log(message: String)
}
```

### Step 2: Implement actual in each platform

```kotlin
// androidMain
actual class PlatformLogger {
    actual fun log(message: String) {
        Log.d("App", message)
    }
}

// iosMain (or iosArm64Main / iosSimulatorArm64Main)
actual class PlatformLogger {
    actual fun log(message: String) {
        NSLog("%@", message)
    }
}
```

### Step 3: Provide via a module

```kotlin
// commonMain — interface used everywhere
interface Logger {
    fun log(message: String)
}

// androidMain — module
@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {
    @Provides
    @Singleton
    fun provideLogger(): Logger = PlatformLogger()
}

// iosMain — same module, different actual
@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {
    @Provides
    @Singleton
    fun provideLogger(): Logger = PlatformLogger()
}
```

Each platform has its own `LoggingModule` that provides the platform-specific `PlatformLogger`. The shared code depends only on the `Logger` interface; it doesn't know about `Log` or `NSLog`.

---

## Alternative: Single Module with expect/actual Construction

If construction is simple, you can keep the module in `commonMain` and use an expect/actual function:

```kotlin
// commonMain
expect fun createPlatformLogger(): Logger

@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {
    @Provides
    @Singleton
    fun provideLogger(): Logger = createPlatformLogger()
}

// androidMain
actual fun createPlatformLogger(): Logger = PlatformLogger()

// iosMain
actual fun createPlatformLogger(): Logger = PlatformLogger()
```

---

## Next Steps

- **[KMP Overview](overview)** — Source sets and KSP targets
- **[Platform-Specific Setup](../installation/platform-specific)** — iOS KSP setup, Android init
