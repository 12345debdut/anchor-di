# Creating Custom Scopes

Use custom scopes when built-in scopes don't match your lifecycle — e.g. one scope per Activity, per user session, or per flow.

## 1. Define the Scope Marker

Use a top-level `object` (or class). It identifies the scope; KSP and runtime use its qualified name.

```kotlin
object ActivityScope
```

## 2. Provide Bindings for That Scope

**Option A — Module with `@InstallIn(YourScope::class)`:**

```kotlin
@Module
@InstallIn(ActivityScope::class)
object ActivityModule {
    @Provides
    fun provideNavigator(): ScreenNavigator = ScreenNavigatorImpl()
}
```

**Option B — `@Scoped(YourScope::class)` on a class:**

```kotlin
@Scoped(ActivityScope::class)
class ActivityScopedAnalytics @Inject constructor(
    private val navigator: ScreenNavigator
)
```

## 3. Enter the Scope and Resolve

You must **enter** the scope before resolving types bound to it.

### Pattern A — Temporary Scope (`withScope`)

The scope exists only for the duration of the block.

```kotlin
Anchor.withScope(ActivityScope::class) { scope ->
    val navigator = scope.get<ScreenNavigator>()
    val analytics = scope.get<ActivityScopedAnalytics>()
    // use them; scope ends when block exits
}
```

### Pattern B — Long-lived Scope (`scopedContainer`)

Hold a container that *is* the scope; release it when the lifecycle ends (e.g. when Activity is destroyed).

```kotlin
// e.g. in Activity.onCreate()
val activityScope = Anchor.scopedContainer(ActivityScope::class)

// Later:
val navigator = activityScope.get<ScreenNavigator>()

// When Activity is destroyed, stop holding activityScope
```

## Rules

| Rule | Reason |
|------|--------|
| Scope type must be **top-level** | KSP and runtime use the qualified name as scope ID |
| Resolve scoped types only inside `withScope` or from `scopedContainer` | Root has no scope; scoped bindings throw otherwise |
| You own the lifecycle | Nothing auto-creates or destroys the scope |
