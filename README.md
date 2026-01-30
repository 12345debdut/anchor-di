# ⚓ Anchor-DI

**Anchor-DI** is a **compile-time dependency injection framework for Kotlin Multiplatform (KMP)** with first-class support for **Compose Multiplatform (CMP)**.

It brings a **Hilt / Dagger–like developer experience** to KMP while remaining:
- 🚫 Reflection-free
- ⚡ Compile-time validated
- 🌍 Fully multiplatform (Android, iOS, Desktop, Web)
- 🎨 Compose-first

---

## ✨ Why Anchor-DI?

Dependency Injection in Kotlin Multiplatform is still a hard problem.

| Existing Solution | Limitation |
|------------------|------------|
| Koin | Runtime DI, slower startup, runtime failures |
| Hilt / Dagger | Android-only |
| Manual DI | Boilerplate-heavy, error-prone |
| Reflection-based DI | Not multiplatform-safe |

**Anchor-DI solves this by shifting all DI logic to compile time.**

---

## 🎯 Design Principles

- **Compile-time dependency graph**
- **No Service Locator**
- **No runtime reflection**
- **Strict validation**
- **Predictable behavior**
- **Multiplatform by design**

If it compiles — it works.

---

## 🧱 High-Level Architecture

Anchor-DI uses **KSP (Kotlin Symbol Processing)** to analyze your source code and generate a **static dependency graph** during compilation.

### What gets validated at compile time:
- Missing bindings
- Dependency cycles
- Scope violations
- Duplicate providers
- Invalid multibindings

💥 Any violation fails the build.

---

## 🌍 Multiplatform First

- Generated code lives in `commonMain`
- No JVM-only APIs
- Platform-specific dependencies use `expect / actual`

```kotlin
expect class PlatformContext
```

```kotlin
@Module
object PlatformModule {
    @Provides
    fun providePlatformContext(): PlatformContext
}
```

---

## 🧩 Core Concepts

### Modules

Modules define how objects are created.

```kotlin
@Module
object NetworkModule {

    @Singleton
    @Provides
    fun provideHttpClient(): HttpClient = HttpClient()
}
```

---

### Constructor Injection

```kotlin
class UserRepository @Inject constructor(
    private val api: UserApi
)
```

---

### Components

Components are **entry points** into the dependency graph.

```kotlin
@Component
interface AppComponent {
    fun userRepository(): UserRepository
}
```

Anchor-DI generates the implementation at compile time.

---

## 🔐 Scoping Model

Anchor-DI supports explicit, validated scopes:

- `@Singleton`
- `@ScreenScope`
- `@ViewModelScope`
- Custom scopes

```kotlin
@ScreenScope
class HomeViewModel @Inject constructor(
    private val repository: UserRepository
)
```

### Compile-Time Scope Rules
- ❌ No injecting short-lived dependencies into long-lived scopes
- ❌ No scope cycles
- ❌ No scope leaks

---

## 🔀 Multibinding

Supports **Set** and **Map** multibindings (Dagger-style).

### Into Set

```kotlin
@IntoSet
@Provides
fun provideAnalyticsTracker(): Tracker
```

### Into Map

```kotlin
@IntoMap
@StringKey("firebase")
@Provides
fun provideFirebaseTracker(): Tracker
```

---

## 🎨 Compose Multiplatform Integration

Designed for **Compose Multiplatform** from day one:

- App-level container created once
- Scoped containers per Screen / ViewModel
- Works with:
    - Android process recreation
    - iOS lifecycle boundaries
    - Desktop recomposition

No reflection. No magic. Only generated code.

---

## 🔄 Build Flow

1. Developer writes annotated code
2. KSP runs during compilation
3. Anchor-DI generates:
    - Factories
    - Containers
    - Scope holders
4. App uses generated code directly

**Runtime overhead is near zero.**

---

## 🚀 Benefits

- ⚡ Faster startup
- 🧠 Compile-time safety
- 🧩 Deterministic dependency graphs
- 📦 Minimal runtime footprint
- 🌍 True Kotlin Multiplatform support

---

## 🛠️ Project Status

🚧 **Early-stage / active development**

Planned milestones:
- Core annotation API
- KSP validation engine
- Multibinding implementation
- Compose lifecycle integration
- Maven Central publishing
- Documentation & samples

---

## 🧪 Who Should Use Anchor-DI?

- Kotlin Multiplatform SDK authors
- Compose Multiplatform applications
- Performance-sensitive apps
- Teams wanting predictable DI
- Android developers missing Hilt on iOS 😉

---

## 🤝 Contributing

Contributions, RFCs, and discussions are welcome.

This project aims to become a **foundational DI solution for Kotlin Multiplatform**.

---

## 📜 License

```
TBD
```
