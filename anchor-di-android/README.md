# anchor-di-android

Android-only, **Compose-free** module for Anchor DI. Use when your KMP app has an Android target but does **not** use Compose (e.g. Views, or shared logic only).

## What it provides

- **ActivityScope** — Scope for bindings tied to an Activity lifecycle. Use with `Anchor.withScope(ActivityScope::class) { ... }` or `Anchor.scopedContainer(ActivityScope::class)`.

Same package as in anchor-di-compose (`com.debdut.anchordi.compose.ActivityScope`) so CMP users who depend on anchor-di-compose get the same type (anchor-di-compose depends on anchor-di-android).

## Dependency

```kotlin
// Android source set only
androidMain.dependencies {
    implementation("io.github.12345debdut:anchor-di-android:0.1.0")
}
// Also need anchor-di-api and anchor-di-core in commonMain
```

## Usage

```kotlin
@Scoped(ActivityScope::class)
class MyActivityScoped @Inject constructor(...)

// In Activity:
Anchor.withScope(ActivityScope::class) { scope ->
    val thing = scope.get<MyActivityScoped>()
}
```

**ViewModel scope (Android, no Compose):** Use `getViewModelScope(owner, lifecycle)` or `viewModelScope()` so ViewModel-scoped bindings are tied to the Activity/Fragment lifecycle and disposed automatically. See [docs/VIEWMODEL_ALL_PLATFORMS.md](../docs/VIEWMODEL_ALL_PLATFORMS.md).

For Compose Multiplatform, use **anchor-di-compose** (which includes ActivityScope via anchor-di-android).
