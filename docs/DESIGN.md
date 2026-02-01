# Anchor DI - Architecture & Design Document

> A production-grade, Hilt-inspired Dependency Injection library for Kotlin Multiplatform (KMP/CMP) using KSP for compile-time code generation.

---

## 1. Architecture Overview

### 1.1 Design Philosophy

Anchor DI aims to provide:
- **Compile-time safety** — Fail fast at compile time, not runtime
- **Zero reflection** — Pure generated code, works on iOS/Native targets
- **Hilt-like API** — Familiar to Android developers, easy adoption
- **KMP-first** — Designed for `commonMain`, with platform-specific extensions where needed

### 1.2 Module Structure

```
anchor-di/
├── anchor-di-api/          # Annotations & public API (commonMain)
├── anchor-di-core/      # Runtime container & core logic (commonMain + expect/actual)
├── anchor-di-ksp/          # KSP processor (JVM only, runs at compile time)
└── anchor-di-compose/      # Compose Multiplatform extensions (anchorInject, viewModelAnchor)
```

**Why this structure?**
- **api** — Minimal dependency surface; consumers only need annotations
- **runtime** — Shared container logic; expect/actual for platform-specific initialization
- **ksp** — Processor runs on JVM during Gradle build; generates platform-specific code
- **compose** — Optional module for anchorInject, viewModelAnchor, ActivityScope.

### 1.3 Data Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                         COMPILE TIME                                  │
├─────────────────────────────────────────────────────────────────────┤
│  User Code                    KSP Processor              Generated   │
│  (@Inject, @Module)    →     (Symbol Processing)    →    *_Binding.kt │
│  @Component, @Scope          (Graph Resolution)           *_Factory   │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         RUNTIME                                       │
├─────────────────────────────────────────────────────────────────────┤
│  Generated Factories  →  AnchorContainer  →  @Inject resolution       │
│  (create instances)      (holds scopes)      (field/method injection) │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.4 KSP Multiplatform Setup

Per [KSP multiplatform docs](https://kotlinlang.org/docs/ksp-multiplatform.html):

```kotlin
dependencies {
    add("kspCommonMainMetadata", "com.debdut:anchor-di-ksp:x.y.z")
    add("kspAndroid", "com.debdut:anchor-di-ksp:x.y.z")
    add("kspIosArm64", "com.debdut:anchor-di-ksp:x.y.z")
    add("kspIosSimulatorArm64", "com.debdut:anchor-di-ksp:x.y.z")
}
```

The processor runs for each target; generated code lives in `build/generated/ksp/<sourceSet>/kotlin/`.

**iOS source set note:** KSP generates into target-specific dirs (e.g. `iosSimulatorArm64Main`), which `iosMain` cannot see. Put your `getAnchorContributors()` actual in `iosArm64Main` and `iosSimulatorArm64Main` (not `iosMain`) so it compiles with the generated code.

---

## 2. Core Concepts

### 2.1 Components (Scopes / Containers)

A **Component** is a scope that holds instances. Unlike Hilt's Android-tied components, we define **generic** scopes that work across platforms. For how scope resolution works and lessons learned (e.g. custom components), see **[SCOPES_AND_CUSTOM_COMPONENTS.md](SCOPES_AND_CUSTOM_COMPONENTS.md)**.

| Component     | Scope           | Lifetime                        |
|---------------|-----------------|----------------------------------|
| `Singleton`   | Application     | Process lifetime                 |
| `Scoped`      | Custom          | User-defined (e.g. screen, flow) |
| `Unscoped`    | Per-request     | New instance every time          |

**Platform extensions (anchor-di-compose):**
- `anchorInject()`, `viewModelAnchor()` — Compose Multiplatform (commonMain, works on Android/iOS/Desktop)
- `ActivityScope` — Android Activity scope (androidMain)

### 2.2 Annotations (API)

```kotlin
// Binding
@Inject constructor(...)           // Constructor injection
@Binds                            // Interface → implementation
@Provides                          // Custom factory in module

// Scoping
@Singleton                         // One instance per container
@Scoped(MyScope::class)            // Custom scope

// Modules
@Module
@InstallIn(SingletonComponent::class)
object AppModule { ... }

// Qualifiers
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Named(val value: String)

// Entry point (injection target)
@Inject lateinit var service: MyService
```

### 2.3 Graph Resolution

1. **Collect** — KSP finds all `@Module`, `@Inject` constructors, `@Binds`, `@Provides`
2. **Resolve** — Build dependency graph, detect cycles, validate qualifiers
3. **Generate** — Emit factory classes and component implementations
4. **Register** — Generated code registers with `AnchorContainer` at init

---

## 3. Feature List

### Phase 1 — MVP (Must Have)

| Feature              | Description                                      | Priority |
|----------------------|--------------------------------------------------|----------|
| Constructor injection| `@Inject` on primary constructor                 | P0       |
| Singleton scope      | `@Singleton` — one instance per app              | P0       |
| Modules              | `@Module` + `@InstallIn`                         | P0       |
| @Provides            | Manual provision in modules                      | P0       |
| @Binds               | Interface → impl binding                         | P0       |
| Qualifiers           | `@Named("key")` or custom qualifiers             | P0       |
| Container init       | `Anchor.init()` at app startup                   | P0       |
| `Anchor.inject<T>()` | Get instance from container                      | P0       |
| KMP support          | commonMain, androidMain, iosMain                 | P0       |

### Phase 2 — Production Grade

| Feature              | Description                                      | Priority |
|----------------------|--------------------------------------------------|----------|
| Custom scopes        | `@Scoped(MyScope::class)`                        | P1       |
| Lazy injection       | `Lazy<T>` for deferred resolution                | P1       |
| Provider injection   | `Provider<T>` for multiple instances             | P1       |
| Compile-time validation | Missing bindings, cycles, ambiguous bindings  | P1       |
| Component dependencies | Child components with parent scope            | P1       |
| Multi-module support | Cross-module dependency resolution               | P1       |

### Phase 3 — Polish & Extensions

| Feature              | Description                                      | Priority |
|----------------------|--------------------------------------------------|----------|
| Android ViewModel    | `@HiltViewModel`-like for ViewModels             | P2       |
| Android Activity scope | Activity-scoped components                     | P2       |
| Compose integration  | `@Composable` with injection                     | P2       |
| Test support         | Test containers, overrides                       | P2       |
| Error messages       | Clear, actionable compile errors                 | P2       |
| Documentation        | Full API docs, migration guides                  | P2       |

### Phase 4 — Ecosystem

| Feature              | Description                                      | Priority |
|----------------------|--------------------------------------------------|----------|
| Maven/Gradle publish | BOM, version catalog                             | P3       |
| Sample app           | Full KMP + CMP sample                            | P3       |
| Benchmarks           | Startup, injection overhead                      | P3       |
| Contributing guide   | For external contributors                        | P3       |

---

## 4. Generated Code Shape

Generated code is split into multiple files aligned with the modules and classes you define:

- **`AnchorGenerated_<moduleId>_<ModuleName>_Factories.kt`** / **`_Inject_Factories.kt`** — Internal factory classes for `@Inject` types, split by the same grouping as contributors (one file per module that has @Inject impls, plus `_Inject_Factories.kt` for the rest) so factory files don’t bloat.
- **`AnchorGenerated_<moduleId>_<ModuleName>.kt`** — One `ComponentBindingContributor` per `@Module` class (e.g. `AppModule`, `LoggerModule`). Each file contains only the bindings from that module: its `@Provides`, `@Binds`, and any `@Inject` class that is the implementation of a `@Binds` in that module only.
- **`AnchorGenerated_<moduleId>_Inject.kt`** — Bindings for `@Inject` classes that are not exclusively bound by a single module (e.g. ViewModels, standalone singletons).
- **`AnchorGenerated_<moduleId>.kt`** — Single aggregator that implements `ComponentBindingContributor` and delegates to each per-module and Inject contributor. App code continues to use only this object (e.g. `Anchor.init(AnchorGenerated_composeapp)`).

This keeps generated files aligned with your module structure and avoids one large monolithic file.

### 4.1 Module Binding (example)

**User code:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApi(): Api = ApiImpl()
}
```

**Generated (simplified):**
```kotlin
object AppModule_SingletonComponentBindings : ComponentBindingContributor {
    override fun contribute(registry: BindingRegistry) {
        registry.register(
            key = Key.of<Api>(),
            scope = SingletonScope,
            factory = { c -> (AppModule.provideApi()) }
        )
    }
}
```

### 4.2 Constructor Injection

**User code:**
```kotlin
class Repository @Inject constructor(
    private val api: Api
)
```

**Generated:**
```kotlin
object Repository_Factory : Factory<Repository> {
    override fun create(container: AnchorContainer): Repository {
        val api = container.get<Api>()
        return Repository(api)
    }
}
```

---

## 5. Platform Considerations

### 5.1 iOS / Native

- **No reflection** — All resolution via generated factories ✅
- **No ClassLoader** — Use `typeOf<T>()` or generated registration
- **Init timing** — App must call `Anchor.init()` before any injection

### 5.2 Android

- **Application.onCreate** — Ideal place for `Anchor.init()`
- **ProGuard/R8** — Generated code is direct; ensure keep rules for public API

### 5.3 JS (if we add later)

- **Tree-shaking** — Unused bindings can be eliminated
- **No reflection** — Same as native

---

## 6. Naming & Package

- **Library name:** Anchor DI
- **Package:** `com.debdut.anchordi`
- **Maven coords:** `com.debdut:anchor-di-api`, etc.

---

## 6.1 Multi-Module Setup

For projects with multiple modules using Anchor DI:

1. **Set unique module ID per module** in each module's `build.gradle.kts`:
   ```kotlin
   ksp {
       arg("anchorDiModuleId", "myfeature")  // Unique per module
   }
   ```
   This generates `AnchorGenerated_myfeature` instead of `AnchorGenerated` to avoid name clashes.

2. **Combine contributors at app init**:
   ```kotlin
   // In app's getAnchorContributors:
   actual fun getAnchorContributors() = arrayOf(
       AnchorGenerated_composeapp,
       AnchorGenerated_myfeature
   )
   ```

---

## 7. Implementation Order

1. **anchor-di-api** — Annotations only
2. **anchor-di-core** — `AnchorContainer`, `Key`, `Scope`, `BindingRegistry`
3. **anchor-di-ksp** — Processor for `@Inject` constructor → factory
4. **anchor-di-ksp** — Processor for `@Module`, `@Provides`, `@Binds`
5. **anchor-di-ksp** — Graph validation, qualifiers, scopes
6. **Integration** — Wire into composeApp, verify end-to-end
7. **anchor-di-compose** — Compose Multiplatform extensions

---

*Document version: 1.0 | Last updated: Jan 31, 2025*
