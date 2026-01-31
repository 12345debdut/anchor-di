# Anchor DI Documentation

## Quick Start

### 1. Add dependencies

```kotlin
// build.gradle.kts
plugins {
    id("com.google.devtools.ksp") version "2.3.5"
}

dependencies {
    implementation(project(":anchor-di-api"))
    implementation(project(":anchor-di-runtime"))
    implementation(project(":anchor-di-compose"))  // For anchorInject(), viewModelAnchor()
    add("kspCommonMainMetadata", project(":anchor-di-ksp"))
    add("kspAndroid", project(":anchor-di-ksp"))
    add("kspIosArm64", project(":anchor-di-ksp"))
    add("kspIosSimulatorArm64", project(":anchor-di-ksp"))
}
```

### 2. Define dependencies

```kotlin
// Repository with @Inject
@Singleton
class UserRepository @Inject constructor(
    private val api: Api
)

// Interface binding
@Module
@InstallIn(SingletonComponent::class)
abstract class ApiModule {
    @Binds @Singleton
    abstract fun bindApi(impl: ApiImpl): Api
}

// Manual provision
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideApi(): Api = ApiImpl()
}
```

### 3. Initialize at startup

```kotlin
// Android: Application.onCreate() or before first composable
Anchor.init(*getAnchorContributors())
```

### 4. Inject

```kotlin
val repository = Anchor.inject<UserRepository>()
```

## Annotations

| Annotation | Use |
|------------|-----|
| `@Inject` | Constructor injection |
| `@Singleton` | One instance per app |
| `@Scoped(Scope::class)` | One instance per scope |
| `@ViewModelScoped` | One instance per ViewModel (same as `@Scoped(ViewModelComponent::class)`) |
| `@NavigationScoped` | One instance per navigation destination (Compose; use with NavigationScopedContent + navigationScopedInject()) |
| `@Module` | Declares a module |
| `@InstallIn(SingletonComponent::class)` | Where module is installed (app-wide) |
| `@InstallIn(ViewModelComponent::class)` | Where module is installed (ViewModel scope) |
| `@InstallIn(NavigationComponent::class)` | Where module is installed (navigation destination scope) |
| `@Provides` | Manual factory in module |
| `@Binds` | Interface → implementation |
| `@Named("id")` | Qualifier for disambiguation |

## Architecture

- **anchor-di-api**: Annotations only (`@Inject`, `@Module`, `@Provides`, `@Binds`, `@InstallIn`, `@Singleton`, `@Scoped`, `@ViewModelScoped`, `@NavigationScoped`, `@Named`, `@Qualifier`). No runtime dependency.
- **anchor-di-runtime**: Container ([Anchor], [AnchorContainer]), [Key], [Binding] (Unscoped/Singleton/Scoped), [Factory], [ComponentBindingContributor]. Resolves dependencies at runtime; no reflection in hot path (uses `reified` and generated code).
- **anchor-di-ksp**: Symbol processor that discovers `@Inject` classes and `@Module` classes, validates (missing bindings, circular dependencies, @Binds shape), and generates a [ComponentBindingContributor] implementation that registers all bindings.
- **anchor-di-compose**: Compose helpers: `anchorInject()`, `viewModelAnchor()`, `NavigationScopedContent` + `navigationScopedInject()` for navigation-scoped bindings (Android).

Data flow: **annotations** → **KSP** generates contributor → **Anchor.init(contributors)** builds container → **inject / withScope** resolve from container.

## Advanced

### Lazy & Provider

```kotlin
class MyClass @Inject constructor(
    private val api: Lazy<Api>,           // Resolved on first access
    private val factory: AnchorProvider<Api>  // New/get on each get()
)
```

### Custom scopes and custom components

Use `@Scoped(Scope::class)` and `Anchor.withScope(Scope::class) { scope -> ... }` for one-off use.

**Custom components:** You can install modules in a custom scope with `@InstallIn(MyScope::class)` (use a top-level `object` or class). Enter the scope with `Anchor.withScope(MyScope::class) { ... }` or hold it with `Anchor.scopedContainer(MyScope::class)` and release the reference when the scope should end.

```kotlin
object ActivityScope

@Module
@InstallIn(ActivityScope::class)
object ActivityModule { @Provides fun provideX(): X = ... }

// One-off:
Anchor.withScope(ActivityScope::class) { scope -> scope.get<X>() }

// Hold and manage lifecycle:
val scope = Anchor.scopedContainer(ActivityScope::class)
scope.get<X>()
// when done, let scope go out of scope
```

For how scope resolution works and built-in vs custom components, see [SCOPES_AND_CUSTOM_COMPONENTS.md](SCOPES_AND_CUSTOM_COMPONENTS.md). For a **session-like scope with logout** (dispose current scope and create a new one, without CompositionLocal), see [SESSION_AND_LOGOUT.md](SESSION_AND_LOGOUT.md) and the sample app.

### ViewModel (Compose Multiplatform)

Use **ViewModelComponent** for bindings that should live as long as a ViewModel (e.g. screen state, use-case per screen). Create ViewModels with **viewModelAnchor()** so resolution runs inside the ViewModel scope.

```kotlin
// Module installed in ViewModel scope
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    @Provides
    fun provideScreenState(): ScreenState = ScreenState()
}

@AnchorViewModel
class MyViewModel @Inject constructor(
    private val repo: Repository,
    private val screenState: ScreenState  // same instance for this ViewModel
) : ViewModel()

@Composable
fun Screen(viewModel: MyViewModel = viewModelAnchor()) { ... }
```

**Important:** ViewModel-scoped types must only be requested **inside** the ViewModel scope. Use `viewModelAnchor()` to create ViewModels; do not use `viewModel { Anchor.inject<MyViewModel>() }` (that would resolve outside the scope and fail for ViewModel-scoped dependencies).

### Compose Multiplatform (commonMain)

```kotlin
@Composable
fun Screen(repo: Repository = anchorInject()) { ... }

@Composable
fun Screen(viewModel: MyViewModel = viewModelAnchor()) { ... }
```

`anchorInject()` and `viewModelAnchor()` work in **commonMain** across Android, iOS, and Desktop.

### Navigation-scoped (Compose, Android)

For **Compose Navigation** (Jetpack Navigation Compose), use **NavigationComponent** for bindings that live per destination (one instance per `NavBackStackEntry`, cleared when the destination is popped). Wrap destination content in `NavigationScopedContent(navBackStackEntry)` and use `navigationScopedInject<T>()` inside it.

```kotlin
@NavigationScoped
class ScreenState @Inject constructor() { ... }

NavHost(navController, startDestination = "home") {
    composable("home") {
        NavigationScopedContent(requireNotNull(it)) {
            val state = navigationScopedInject<ScreenState>()
            HomeScreen(state)
        }
    }
}
```

Add `androidx.navigation:navigation-compose` to the **androidMain** dependencies of the module that uses `NavigationScopedContent` (anchor-di-compose already depends on it for Android).

## Multi-module

```kotlin
ksp { arg("anchorDiModuleId", "myfeature") }
// Generates AnchorGenerated_myfeature

Anchor.init(AnchorGenerated_app, AnchorGenerated_feature)
```

## iOS Setup

KSP generates code into target-specific dirs that `iosMain` cannot see. Put your `getAnchorContributors()` actual in **both** `iosArm64Main` and `iosSimulatorArm64Main` (not `iosMain`):

```
src/iosArm64Main/kotlin/.../AnchorSetup.ios.kt
src/iosSimulatorArm64Main/kotlin/.../AnchorSetup.ios.kt
```

### Running the iOS app in Xcode

1. **Open the project:** Open `iosApp/iosApp.xcodeproj` in Xcode (not a workspace).
2. **Code signing (device / archive):** Set your Apple Developer Team ID so Xcode can sign the app:
   - In **iosApp/Configuration/Config.xcconfig**, set `TEAM_ID=<your_team_id>`, or  
   - In Xcode: select the **iosApp** target → **Signing & Capabilities** → choose your **Team**.
   - You can find your Team ID in [Apple Developer](https://developer.apple.com/account) → Membership, or in Xcode when selecting a team.
3. **Build and run:** Choose a simulator or a connected device, then **Product → Run** (or ⌘R).  
   The **Compile Kotlin Framework** run script runs first (it runs `:composeApp:embedAndSignAppleFrameworkForXcode`), then Xcode builds and links the app.
4. **If the script is skipped:** Ensure **User Script Sandboxing** is disabled: Project → **Build Settings** → search “User Script Sandboxing” → set to **No** for the project or target.

## Troubleshooting

### "Scoped binding for X requires a scope"

This means a type is bound to a scope (e.g. **ViewModelComponent**) but was requested from the **root** container (no scope active).

**Fix:**

- **ViewModel-scoped types:** Create the ViewModel with **viewModelAnchor()**, not `viewModel { Anchor.inject<MyViewModel>() }`. Only `viewModelAnchor()` runs resolution inside `Anchor.withScope(ViewModelComponent::class)`.
- **Other scopes:** Resolve the type only inside `Anchor.withScope(YourScope::class) { scope -> scope.get<X>() }`.
- **Alternative:** If the type does not need to be scoped, install its module in **SingletonComponent** instead of ViewModelComponent.

### "No binding found for X"

Add a binding (e.g. `@Inject` constructor, `@Provides` in a module, or `@Binds`), ensure the module is `@InstallIn(SingletonComponent::class)` or `@InstallIn(ViewModelComponent::class)`, and **rebuild** (KSP runs at compile time).
