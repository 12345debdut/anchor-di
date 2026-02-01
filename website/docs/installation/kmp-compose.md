# KMP + Compose Multiplatform

Use Anchor DI in a **Kotlin Multiplatform** project with **Compose Multiplatform** for UI.

## Dependencies

```kotlin
// shared/build.gradle.kts (or your compose module)
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.12345debdut:anchor-di-api:0.1.0")
            implementation("io.github.12345debdut:anchor-di-core:0.1.0")
            implementation("io.github.12345debdut:anchor-di-compose:0.1.0")
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosSimulatorArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
}
```

## What You Get

| Feature | API |
|---------|-----|
| Singleton / unscoped injection in Composables | `anchorInject<T>()` |
| ViewModel injection with scope | `viewModelAnchor()` |
| Navigation-scoped injection (Android) | `NavigationScopedContent` + `navigationScopedInject()` |
| ViewModel-scoped bindings | `@InstallIn(ViewModelComponent::class)` |
| `@AnchorViewModel` | Create ViewModels with correct scope |

## Initialize

```kotlin
// commonMain - call once at app startup
@Composable
fun App() {
    DisposableEffect(Unit) {
        Anchor.init(*getAnchorContributors())
        onDispose { }
    }
    // or initialize in platform entry before first Composable
    AppContent()
}
```

## Usage in Composables

```kotlin
@Composable
fun UserScreen(
    repository: UserRepository = anchorInject(),  // Singleton
    viewModel: UserViewModel = viewModelAnchor()  // ViewModel-scoped
) {
    // ...
}
```
