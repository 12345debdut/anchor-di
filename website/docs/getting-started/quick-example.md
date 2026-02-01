# Quick Example

A minimal Anchor DI setup in four steps.

## 1. Add Dependencies

```kotlin
// build.gradle.kts (shared module)
plugins {
    id("com.google.devtools.ksp") version "2.3.5"
}

dependencies {
    implementation("io.github.12345debdut:anchor-di-api:0.1.0")
    implementation("io.github.12345debdut:anchor-di-core:0.1.0")
    implementation("io.github.12345debdut:anchor-di-compose:0.1.0")  // For Compose
    add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosSimulatorArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
}
```

## 2. Define Dependencies

```kotlin
// Api.kt
interface UserApi {
    suspend fun getUser(id: String): User
}

// UserRepository.kt
@Singleton
class UserRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun loadUser(id: String) = api.getUser(id)
}

// ApiModule.kt
@Module
@InstallIn(SingletonComponent::class)
interface ApiModule {
    @Binds
    @Singleton
    fun bindUserApi(impl: UserApiImpl): UserApi
}
```

## 3. Initialize at Startup

```kotlin
// Android: Application.onCreate() or before first composable
// iOS: App init
Anchor.init(*getAnchorContributors())
```

## 4. Inject

```kotlin
// Imperative
val repository = Anchor.inject<UserRepository>()

// Compose
@Composable
fun UserScreen(
    repository: UserRepository = anchorInject()
) {
    // use repository
}
```

That's it. KSP generates the wiring; you write declarative code.
