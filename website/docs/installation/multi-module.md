# Multi-Module Setup

When your KMP project has **multiple modules** that each contribute DI bindings (e.g. feature modules, data module, domain module), you need to configure KSP so each module generates its own contributor and aggregate them at the app level. This page explains how.

---

## Why Multi-Module?

In a large project, you might have:

- **app** — Entry point; aggregates all contributors
- **feature-auth** — Auth feature with its own bindings
- **feature-home** — Home feature with its own bindings
- **data** — Repositories, API
- **domain** — Use cases

Each module that has `@Inject` classes or `@Module` classes needs to generate a contributor. You then aggregate them in the app module and pass them to `Anchor.init()`.

---

## Step 1: Per-Module KSP Option

In each feature or shared module that has DI bindings, add the KSP plugin and the **module ID** option:

```kotlin
// feature-auth/build.gradle.kts
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("io.github.12345debdut:anchor-di-api:0.1.0")
    implementation("io.github.12345debdut:anchor-di-core:0.1.0")
    add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspIosSimulatorArm64", "io.github.12345debdut:anchor-di-ksp:0.1.0")
}

ksp {
    arg("anchorDiModuleId", "auth")
}
```

This generates `AnchorGenerated_auth` instead of the default `AnchorGenerated`. Each module should have a **unique** `anchorDiModuleId` (e.g. `auth`, `home`, `data`, `app`).

---

## Step 2: Aggregate Contributors in App Module

In your **app module**, collect all contributors and pass them to `Anchor.init()`:

```kotlin
// app — commonMain (expect)
expect fun getAnchorContributors(): Array<ComponentBindingContributor>

// app — androidMain (actual)
actual fun getAnchorContributors(): Array<ComponentBindingContributor> = arrayOf(
    AnchorGenerated_app,
    AnchorGenerated_auth,
    AnchorGenerated_home,
    AnchorGenerated_data
)

// app — iosArm64Main and iosSimulatorArm64Main (actual)
actual fun getAnchorContributors(): Array<ComponentBindingContributor> = arrayOf(
    AnchorGenerated_app,
    AnchorGenerated_auth,
    AnchorGenerated_home,
    AnchorGenerated_data
)
```

**Order:** The order of contributors can matter if modules override bindings (though typically you avoid overrides). List them in a consistent order (e.g. app, then features, then data).

---

## Step 3: Module Dependencies

- Each module that contributes bindings needs `anchor-di-api`, `anchor-di-core`, and `anchor-di-ksp`.
- The **app module** must depend on all feature/data modules so the generated `AnchorGenerated_*` classes are on the classpath.
- Use the same `anchorDiModuleId` for a given module across all targets (commonMain, android, ios, etc.).

---

## Example Layout

```
project/
  app/           — anchorDiModuleId: "app"
  feature-auth/  — anchorDiModuleId: "auth"
  feature-home/  — anchorDiModuleId: "home"
  data/          — anchorDiModuleId: "data"
```

`app` depends on `feature-auth`, `feature-home`, and `data`. Each generates its own contributor. The app's `getAnchorContributors()` returns all of them.

---

## Next Steps

- **[Installation Setup](setup)** — Full installation guide
- **[Platform-Specific Setup](platform-specific)** — iOS KSP setup and more
