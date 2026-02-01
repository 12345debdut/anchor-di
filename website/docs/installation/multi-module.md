# Multi-Module Setup

When your KMP project has **multiple modules** that each contribute DI bindings, use the **module ID** KSP option so each module generates its own contributor.

## Per-Module KSP Option

In each feature module that has `@Inject` classes or `@Module` classes:

```kotlin
// feature/build.gradle.kts
ksp {
    arg("anchorDiModuleId", "feature")
}
```

This generates `AnchorGenerated_feature` instead of the default `AnchorGenerated`. Each module should have a **unique** `anchorDiModuleId`.

## Aggregating Contributors

In your app module (or shared entry point), collect all contributors and pass them to `Anchor.init()`:

```kotlin
// app - commonMain
expect fun getAnchorContributors(): Array<ComponentBindingContributor>

// app - androidMain
actual fun getAnchorContributors(): Array<ComponentBindingContributor> = arrayOf(
    AnchorGenerated_app,
    AnchorGenerated_feature,
    AnchorGenerated_auth
)

// app - iosArm64Main / iosSimulatorArm64Main
actual fun getAnchorContributors(): Array<ComponentBindingContributor> = arrayOf(
    AnchorGenerated_app,
    AnchorGenerated_feature,
    AnchorGenerated_auth
)
```

## Module Dependencies

- Each module that contributes bindings needs `anchor-di-api`, `anchor-di-core`, and `anchor-di-ksp`.
- The app module must depend on all feature modules so the generated `AnchorGenerated_*` classes are on the classpath.
- Use the same `anchorDiModuleId` for a given module across all targets (commonMain, android, ios, etc.).
