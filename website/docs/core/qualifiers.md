# Qualifiers

When multiple bindings exist for the same type, use **qualifiers** to distinguish them.

## @Named

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Named("apiBaseUrl")
    fun provideApiBaseUrl(): String = "https://api.example.com"

    @Provides
    @Named("webBaseUrl")
    fun provideWebBaseUrl(): String = "https://example.com"
}

class ApiClient @Inject constructor(
    @Named("apiBaseUrl") private val baseUrl: String
)
```

## Custom Qualifier

Define a qualifier annotation:

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebUrl
```

Use it in modules and constructors:

```kotlin
@Provides
@ApiUrl
fun provideApiBaseUrl(): String = "https://api.example.com"

class ApiClient @Inject constructor(
    @ApiUrl private val baseUrl: String
)
```
