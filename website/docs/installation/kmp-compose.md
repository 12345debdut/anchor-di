# KMP + Compose Multiplatform

Use Anchor DI in a **Kotlin Multiplatform** project with **Compose Multiplatform (CMP)** for UI. This page covers dependencies, initialization, and usage in Composables. If you're building UI with Compose on Android, iOS, Desktop, or Web, this setup is for you.

---

## Dependencies

Add these dependencies to your shared module (the one with `commonMain` and Compose):

```kotlin
// shared/build.gradle.kts (or your compose module)
plugins {
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.12345debdut:anchor-di-api:x.x.x")
            implementation("io.github.12345debdut:anchor-di-core:x.x.x")
            implementation("io.github.12345debdut:anchor-di-compose:x.x.x")
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:x.x.x")
    add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:x.x.x")
    add("kspIosArm64", "io.github.12345debdut:anchor-di-ksp:x.x.x")
    add("kspIosSimulatorArm64", "io.github.12345debdut:anchor-di-ksp:x.x.x")
}
```

**What each dependency does:**

- `anchor-di-api` — Annotations (`@Inject`, `@Module`, etc.).
- `anchor-di-core` — Runtime container and `Anchor` object.
- `anchor-di-compose` — `anchorInject()`, `viewModelAnchor()`, `NavigationScopedContent`, `navigationScopedInject()`.
- `anchor-di-ksp` — KSP processor. Add for each Kotlin target you use.

---

## What You Get

| Feature | API | Use Case |
|---------|-----|----------|
| **Singleton / unscoped injection in Composables** | `anchorInject<T>()` | Inject app-wide singletons (repositories, config) in Composables. |
| **ViewModel injection with scope** | `viewModelAnchor()` | Inject ViewModels and ViewModel-scoped dependencies. |
| **Navigation-scoped injection (Android)** | `NavigationScopedContent` + `navigationScopedInject()` | Scope objects to navigation destinations. |
| **ViewModel-scoped bindings** | `@InstallIn(ViewModelComponent::class)` | Provide types that live per ViewModel. |
| **@AnchorViewModel** | Mark ViewModels for `viewModelAnchor()` | Create ViewModels with correct scope. |

---

## Initialize

Call `Anchor.init(*getAnchorContributors())` **once** at app startup. Two common patterns:

### Option 1: In platform entry (Android, iOS)

```kotlin
// Android: Application.onCreate()
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Anchor.init(*getAnchorContributors())
    }
}

// iOS: In your @main or main()
Anchor.init(*getAnchorContributors())
```

### Option 2: In root Composable (commonMain)

```kotlin
@Composable
fun App() {
    DisposableEffect(Unit) {
        Anchor.init(*getAnchorContributors())
        onDispose { }
    }
    AppContent()
}
```

Use one or the other — don't call `Anchor.init()` multiple times.

---

## Usage in Composables

Inject singletons or unscoped types with `anchorInject()`; inject ViewModels with `viewModelAnchor()`:

```kotlin
@Composable
fun UserScreen(
    repository: UserRepository = anchorInject(),   // Singleton
    viewModel: UserViewModel = viewModelAnchor()   // ViewModel-scoped
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

**Important:** Use `viewModelAnchor()` for ViewModels that need ViewModel-scoped dependencies — not `viewModel { Anchor.inject<ViewModel>() }`. Only `viewModelAnchor()` runs resolution inside the ViewModel scope.

---

## Next Steps

- **[Compose Overview](../compose/overview)** — `anchorInject()` and `viewModelAnchor()` in detail
- **[Installation Setup](setup)** — Full installation guide
- **[Platform-Specific Setup](platform-specific)** — iOS KSP setup and more
