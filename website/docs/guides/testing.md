# Testing

## Test Containers and Overrides

Anchor DI provides **test support** for resetting the container and overriding bindings in tests.

## Anchor.reset()

Call `Anchor.reset()` at the start of each test (or in `@Before`) to clear the container. This allows you to reinitialize with test-specific contributors or to ensure a clean state between tests.

```kotlin
@Test
fun `test user flow`() {
    Anchor.reset()
    Anchor.init(*getAnchorContributors())  // or test contributors
    // ...
}
```

## Overriding Bindings in Tests

In tests, you can initialize with custom contributors that provide mock implementations:

```kotlin
// In your test module, provide mock implementations
@Module
@InstallIn(SingletonComponent::class)
object TestModule {
    @Provides
    @Singleton
    fun provideUserApi(): UserApi = MockUserApi()
}

// In test setup
Anchor.reset()
Anchor.init(AnchorGenerated_app, TestContributor)
```

## Best Practices

1. **Reset before each test** — Avoid leaking state between tests.
2. **Use test modules** — Provide mock or fake implementations instead of real ones.
3. **Don't inject scoped types into longer-lived types** — Keeps tests predictable.
4. **Unit tests** — Inject mocks directly; you may not need the full DI container for pure unit tests.
5. **Integration tests** — Use `Anchor.init()` with test modules and `Anchor.reset()` in teardown.
