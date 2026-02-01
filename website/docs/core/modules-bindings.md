# Modules and Bindings

## Modules

A **module** groups bindings. Use `@Module` on an `object` or `abstract class`, and `@InstallIn(Component::class)` to declare where it is installed.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient { ... }
}
```

## @Provides

Use `@Provides` to manually construct an instance:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApi(): Api = ApiImpl()
}
```

## @Binds

Use `@Binds` for interface → implementation mapping. The method must be `abstract` and the implementation must be injectable:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
interface ApiModule {
    @Binds
    @Singleton
    fun bindApi(impl: ApiImpl): Api
}

class ApiImpl @Inject constructor(...) : Api { ... }
```

## @Inject Constructor

For classes you control, prefer constructor injection:

```kotlin
@Singleton
class UserRepository @Inject constructor(
    private val api: UserApi
)
```

No module is needed; the container creates the instance by resolving the constructor parameters.

## Combining Approaches

You can mix `@Inject`, `@Provides`, and `@Binds`. The container builds a dependency graph; as long as every type has a binding (or is constructible via `@Inject`), resolution succeeds.
