# expect / actual and Platform Modules

Anchor DI works with **expect / actual** for platform-specific dependencies.

## Pattern

1. **Define** an `expect` type in `commonMain`.
2. **Implement** the `actual` type in each platform source set.
3. **Provide** it via a module in a platform-specific source set.

## Example: Platform Logger

```kotlin
// commonMain
expect class PlatformLogger {
    fun log(message: String)
}

// androidMain
actual class PlatformLogger {
    actual fun log(message: String) {
        Log.d("App", message)
    }
}

// iosMain (or iosArm64Main/iosSimulatorArm64Main)
actual class PlatformLogger {
    actual fun log(message: String) {
        NSLog("%@", message)
    }
}
```

## Module with Platform-Specific Provide

```kotlin
// commonMain - interface used everywhere
interface Logger {
    fun log(message: String)
}

// androidMain - module
@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {
    @Provides
    @Singleton
    fun provideLogger(): Logger = PlatformLogger()
}

// iosMain - same module with different actual
@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {
    @Provides
    @Singleton
    fun provideLogger(): Logger = PlatformLogger()
}
```

Each platform has its own `LoggingModule` that provides the platform-specific `PlatformLogger`. The shared code depends only on the `Logger` interface.
