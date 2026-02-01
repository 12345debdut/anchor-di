# Creating Custom Scopes

The built-in scopes (Singleton, ViewModel, Navigation) cover most cases. But sometimes you need a scope that matches **your** lifecycle — for example, one instance per Activity, per user session, or per flow. Anchor DI lets you define **custom scopes** and control when they start and end. This page explains how.

---

## When to Use Custom Scopes

Use custom scopes when:

- **Activity scope** — You want one instance per Android Activity (e.g. a navigator, activity-scoped analytics).
- **Session scope** — You want one instance per user session (e.g. after login, until logout).
- **Flow scope** — You want one instance per flow (e.g. checkout flow, onboarding flow).

Built-in scopes don't cover these because they're tied to Compose/ViewModel or navigation. Custom scopes give you full control.

---

## Step 1: Define the Scope Marker

Use a **top-level** `object` (or class) as the scope marker. It has no methods; it only identifies the scope. KSP and the runtime use its qualified name as the scope ID.

```kotlin
object ActivityScope
```

**Important:** The scope type must be **top-level**. If it's nested (e.g. inside another class), KSP and the runtime may not agree on the scope ID, and you can get "Scoped binding requires a scope" at runtime.

---

## Step 2: Provide Bindings for That Scope

You have two options: a module with `@InstallIn(YourScope::class)` or `@Scoped(YourScope::class)` on a class.

### Option A: Module with @InstallIn

```kotlin
@Module
@InstallIn(ActivityScope::class)
object ActivityModule {
    @Provides
    fun provideNavigator(): ScreenNavigator = ScreenNavigatorImpl()
}
```

Types provided in this module are available only when `ActivityScope` is active. They're created once per scope and cached for the scope's lifetime.

### Option B: @Scoped on a Class

```kotlin
@Scoped(ActivityScope::class)
class ActivityScopedAnalytics @Inject constructor(
    private val navigator: ScreenNavigator
) {
    fun trackScreen(screen: String) { /* ... */ }
}
```

The class is created once per scope when requested inside that scope.

---

## Step 3: Enter the Scope and Resolve

You must **enter** the scope before resolving any type that is bound to it. Two patterns:

### Pattern A: Temporary Scope (`withScope`)

The scope exists only for the duration of the block. When the block exits, the scope ends and cached instances are no longer used.

```kotlin
Anchor.withScope(ActivityScope::class) { scope ->
    val navigator = scope.get<ScreenNavigator>()
    val analytics = scope.get<ActivityScopedAnalytics>()
    // use them; when the block ends, the scope is done
}
```

**Use when:** One-off work (e.g. a single function that needs activity-scoped types).

### Pattern B: Long-Lived Scope (`scopedContainer`)

You get a container that *is* the scope. You keep a reference to it; the scope lives as long as you hold that reference. When your Activity (or screen/session) is destroyed, stop using the container and let it go out of scope.

```kotlin
// e.g. in Activity.onCreate()
val activityScope = Anchor.scopedContainer(ActivityScope::class)

// Later, anywhere you have activityScope:
val navigator = activityScope.get<ScreenNavigator>()

// When Activity is destroyed, stop holding activityScope so it can be GC'd
```

**Use when:** The scope must outlive a single block (e.g. an Activity that injects activity-scoped types in multiple places).

---

## Rules and Gotchas

| Rule | Why |
|------|-----|
| **Scope type must be top-level** | KSP and runtime use the type's qualified name as the scope ID; only top-level types have a stable, matching name. |
| **Resolve scoped types only inside `withScope` or from `scopedContainer`** | If you call `Anchor.inject<ActivityScopedAnalytics>()` from the root (no scope), you get "Scoped binding for X requires a scope." |
| **You own the lifecycle** | Nothing automatically creates or destroys the scope; you call `withScope` or hold/release `scopedContainer`. |

---

## Example: Activity Scope on Android

```kotlin
// Define scope
object ActivityScope

// Provide bindings
@Module
@InstallIn(ActivityScope::class)
object ActivityModule {
    @Provides
    fun provideNavigator(): ScreenNavigator = ScreenNavigatorImpl()
}

// In Activity.onCreate()
class MainActivity : ComponentActivity() {
    private val activityScope = Anchor.scopedContainer(ActivityScope::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigator = activityScope.get<ScreenNavigator>()
        setContent {
            App(navigator = navigator)
        }
    }
    // When Activity is destroyed, activityScope is released
}
```

---

## Next Steps

- **[Built-in Scopes](built-in)** — When to use Singleton, ViewModel, Navigation
- **[Core Concepts](../core/concepts)** — Components and scope rules
