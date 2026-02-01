# Testing

Anchor DI provides **test support** for resetting the container and overriding bindings in tests. This page explains how to use `Anchor.reset()`, provide mock implementations, and follow best practices when testing code that uses Anchor DI.

---

## Why Test Support Matters

In production, your app calls `Anchor.init(*getAnchorContributors())` once at startup and uses real implementations (API, database, etc.). In tests, you often want to:

1. **Reset the container** — Ensure a clean state between tests; avoid leaking state.
2. **Override bindings** — Provide mock or fake implementations instead of real ones (e.g. fake API, in-memory database).

Anchor DI supports both via `Anchor.reset()` and custom contributors.

---

## Anchor.reset()

Call `Anchor.reset()` at the start of each test (or in `@Before`) to clear the container. This allows you to reinitialize with test-specific contributors or ensure a clean state.

```kotlin
@Test
fun `test user flow`() {
    Anchor.reset()
    Anchor.init(*getAnchorContributors())  // or test contributors
    // ... run your test
}
```

**When to use:** Before each test that uses Anchor DI, especially if tests might run in parallel or if you're overriding bindings in some tests.

---

## Overriding Bindings in Tests

In tests, you can initialize with **custom contributors** that provide mock implementations:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object TestModule {
    @Provides
    @Singleton
    fun provideUserApi(): UserApi = FakeUserApi()
}

// In test setup
Anchor.reset()
Anchor.init(AnchorGenerated_app, TestContributor)
```

**How it works:** You define a test module that provides fake or mock implementations. You pass a custom contributor (or a combination of generated and test contributors) to `Anchor.init()`. The container uses your test bindings instead of production ones.

---

## Best Practices

| Practice | Why |
|----------|-----|
| **Reset before each test** | Avoid leaking state between tests; ensure isolation. |
| **Use test modules** | Provide mock or fake implementations instead of real ones; tests run faster and don't hit the network. |
| **Don't inject scoped types into longer-lived types** | Keeps tests predictable; avoids leaks and stale state. |
| **Unit tests: inject mocks directly** | For pure unit tests, you may not need the full DI container; inject mocks directly into the class under test. |
| **Integration tests: use Anchor.init() with test modules** | When testing full flows, use the container with test bindings. Call `Anchor.reset()` in teardown. |

---

## Example: Testing a ViewModel

```kotlin
class UserViewModelTest {

    @Before
    fun setup() {
        Anchor.reset()
        Anchor.init(*getAnchorContributors())  // or with TestModule
    }

    @Test
    fun `loadUser updates uiState`() = runTest {
        val viewModel = UserViewModel(/* inject fake GetUserUseCase */)
        viewModel.loadUser("1")
        // assert uiState
    }
}
```

For ViewModels created with `viewModelAnchor()`, you'd typically use Compose's `viewModel` test utilities or create the ViewModel manually in tests with injected mocks.

---

## Next Steps

- **[Real-World Example](real-world-example)** — Full app structure with DI
- **[Troubleshooting](../troubleshooting)** — Common issues and fixes
