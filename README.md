# Anchor DI

**Hilt-like dependency injection for Kotlin Multiplatform** with compile-time code generation via KSP. Works in `commonMain` across Android, iOS, and Desktop.

---

## Quick start

1. **Add dependencies** (see [docs/README.md](docs/README.md) for full setup):

```kotlin
implementation(project(":anchor-di-api"))
implementation(project(":anchor-di-runtime"))
implementation(project(":anchor-di-compose"))  // For anchorInject(), viewModelAnchor()
add("kspCommonMainMetadata", project(":anchor-di-ksp"))
add("kspAndroid", project(":anchor-di-ksp"))
add("kspIosArm64", project(":anchor-di-ksp"))
add("kspIosSimulatorArm64", project(":anchor-di-ksp"))
```

2. **Define bindings** with `@Inject`, `@Module`, `@Provides`, `@Binds`, `@InstallIn(SingletonComponent::class)` or `@InstallIn(ViewModelComponent::class)`.

3. **Initialize** at app startup: `Anchor.init(*getAnchorContributors())`.

4. **Inject**: `Anchor.inject<T>()`, or in Compose: `viewModelAnchor<MyViewModel>()`, `anchorInject<Repo>()`.

Full API, ViewModel scope, custom scopes, and troubleshooting: **[docs/README.md](docs/README.md)**.

---

## Features

- **Constructor injection** — `@Inject` on primary constructor
- **Singleton & scoped** — `@Singleton`, `@Scoped(Scope::class)`, `@ViewModelScoped`
- **Modules** — `@Module`, `@InstallIn(SingletonComponent::class)` / `@InstallIn(ViewModelComponent::class)` / custom components
- **ViewModel** — `@AnchorViewModel` + `viewModelAnchor()` in Compose Multiplatform
- **Custom components** — `@InstallIn(MyScope::class)` with `Anchor.withScope(MyScope::class)` or `Anchor.scopedContainer(MyScope::class)`
- **KMP** — commonMain, zero reflection, KSP per target

---

## Project structure

```
.
├── anchor-di-api/       # Annotations (Inject, Module, InstallIn, Scoped, …)
├── anchor-di-runtime/   # Container, Anchor, scopes
├── anchor-di-ksp/       # KSP code generator
├── anchor-di-compose/   # anchorInject(), viewModelAnchor(), ActivityScope
├── composeApp/          # Sample app using Anchor DI
├── androidApp/           # Android entry point
├── iosApp/               # iOS entry point
└── docs/                 # DESIGN.md, README, SCOPES_AND_CUSTOM_COMPONENTS.md
```

**Anchor DI in this app:** The [composeApp](composeApp) module uses Anchor DI for repositories, ViewModels, and platform bindings. See `composeApp/src/commonMain/kotlin/.../di/` and [docs/README.md](docs/README.md).

---

## Also included: KMP + AGP 9 template

This repo is built on a **Kotlin Multiplatform** starter aligned with **AGP 9.0**. You get:

- Shared KMP module (`composeApp`) with Compose Multiplatform
- Android app module (`androidApp`), iOS app (`iosApp`)
- Gradle Kotlin DSL, version catalog

See the [Medium article](https://blog.debdut.com/how-to-update-the-android-gradle-plugin-to-version-9-0-0-in-a-kotlin-multiplatform-kmp-agp-9-6a2261a6a8fd) for AGP 9 + KMP details. Use **“Use this template”** on GitHub to create your own project.

---

## Requirements

- Android Studio (recent KMP + AGP 9 tooling)
- Xcode (for iOS)
- JDK compatible with your Gradle / AGP setup

---

## Running the sample

- **Android:** Run the `androidApp` configuration or `./gradlew :androidApp:assembleDebug`.
- **iOS:** Open `iosApp/iosApp.xcodeproj` in Xcode, set your **Team** (or `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`) for code signing, then **Product → Run**. See [docs/README.md#running-the-ios-app-in-xcode](docs/README.md#running-the-ios-app-in-xcode) for details.

---

## License

Apache-2.0 (see [LICENSE](LICENSE)).
