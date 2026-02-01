# Platform-Specific Setup

Anchor DI runs on Android, iOS, Desktop (JVM), and Web (Wasm). Each platform has specific setup requirements — especially iOS, where KSP generates code into target-specific directories. This page covers platform-specific configuration and where to initialize Anchor DI.

---

## iOS: KSP Output and getAnchorContributors

On iOS, KSP generates code into **target-specific directories** that `iosMain` cannot see. You must define `getAnchorContributors()` in **both** `iosArm64Main` and `iosSimulatorArm64Main` (not `iosMain`).

### File Structure

```
src/
  commonMain/kotlin/.../
  iosArm64Main/kotlin/.../AnchorSetup.ios.kt    ← actual here
  iosSimulatorArm64Main/kotlin/.../AnchorSetup.ios.kt  ← and here
  iosMain/kotlin/.../   ← do NOT put getAnchorContributors actual here
```

### Example

```kotlin
// commonMain — expect
expect fun getAnchorContributors(): Array<ComponentBindingContributor>

// iosArm64Main and iosSimulatorArm64Main — actual
actual fun getAnchorContributors(): Array<ComponentBindingContributor> = arrayOf(
    AnchorGenerated
)
```

**Why both?** KSP runs separately for `iosArm64` and `iosSimulatorArm64`. Each target has its own generated code. The `actual` must be in the same source set that compiles with that generated code.

---

## Android: Application.onCreate

Initialize Anchor **before** any UI or dependency resolution. The typical place is `Application.onCreate()`:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Anchor.init(*getAnchorContributors())
    }
}
```

Ensure your `Application` class is registered in `AndroidManifest.xml`:

```xml
<application android:name=".MyApplication" ...>
```

---

## Desktop / JVM

Initialize Anchor **before** creating any UI or calling `Anchor.inject`:

```kotlin
fun main() {
    Anchor.init(*getAnchorContributors())
    application {
        Window(onCloseRequest = ::exitApplication) {
            App()
        }
    }
}
```

---

## Web (Wasm)

Initialize Anchor **before** the first Composable or dependency resolution. Typically in your root Composable or main entry:

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

---

## Quick Reference

| Platform | Where to Init |
|----------|---------------|
| **Android** | `Application.onCreate()` or before first Composable |
| **iOS** | App entry (`@main` or `main()`) before any UI |
| **Desktop (JVM)** | Before `application { }` or first Composable |
| **Web (Wasm)** | In root Composable (`DisposableEffect`) or main entry |

---

## Next Steps

- **[Installation Setup](setup)** — Full installation guide
- **[KMP Overview](../kmp/overview)** — Source sets and KSP targets
- **[expect / actual](../kmp/expect-actual)** — Platform-specific dependencies
