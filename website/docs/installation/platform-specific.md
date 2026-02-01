# Platform-Specific Setup

## iOS: KSP Output and getAnchorContributors

KSP generates code into target-specific directories that `iosMain` cannot see. You must define `getAnchorContributors()` in **both** `iosArm64Main` and `iosSimulatorArm64Main` (not `iosMain`).

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
// commonMain - expect
expect fun getAnchorContributors(): Array<ComponentBindingContributor>

// iosArm64Main and iosSimulatorArm64Main - actual
actual fun getAnchorContributors(): Array<ComponentBindingContributor> = arrayOf(
    AnchorGenerated
)
```

## Android: Application.onCreate

Initialize Anchor before any UI or dependency resolution:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Anchor.init(*getAnchorContributors())
    }
}
```

## Desktop / JVM

Initialize before creating any UI or calling `Anchor.inject`:

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
