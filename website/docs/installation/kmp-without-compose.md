# KMP Without Compose

Use Anchor DI in a **Kotlin Multiplatform** project that does **not** use Compose for UI (e.g. SwiftUI on iOS, Views on Android, or shared logic only).

## Dependencies

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.12345debdut:anchor-di-api:0.1.0")
            implementation("io.github.12345debdut:anchor-di-core:0.1.0")
            implementation("io.github.12345debdut:anchor-di-presentation:0.1.0")  // optional
            implementation("io.github.12345debdut:anchor-di-android:0.1.0")      // Android only
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

## What Works

| Feature | Usage |
|---------|--------|
| Constructor injection | `@Inject`, `@Module`, `@Provides`, `@Binds`, `@InstallIn(SingletonComponent::class)` |
| Singleton scope | `Anchor.inject<T>()` from root |
| Custom scopes | `Anchor.withScope(MyScope::class) { scope -> scope.get<T>() }` |
| ViewModel scope | `ViewModelScopeRegistry.getOrCreate(scopeKey)`; dispose when screen is gone |
| ActivityScope (Android) | `Anchor.withScope(ActivityScope::class) { ... }` |
| Navigation scope | `NavigationScopeRegistry.getOrCreate(scopeKey)` / `dispose(scopeKey)` |
| Lazy / Provider | `Lazy<T>`, `AnchorProvider<T>` injection |
| Multibinding | `@IntoSet`, `@IntoMap`, `Anchor.injectSet<T>()`, `Anchor.injectMap<V>()` |

## What's Different

- **No `anchorInject()` or `viewModelAnchor()`** — These are Compose-only. Use `Anchor.inject<T>()` and manual ViewModel scope.
- **ViewModel scope** — Create with `Anchor.withScope(ViewModelComponent::class) { ... }` or hold a `scopedContainer` per screen and drop it when leaving.
- **Navigation scope** — Use `anchor-di-presentation` for `NavigationScopeRegistry.getOrCreate(scopeKey)` and `dispose(scopeKey)` without Compose.
