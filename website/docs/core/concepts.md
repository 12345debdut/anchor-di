---
sidebar_position: 1
---

# Components and Scopes

## Components

**Components** are entry points into the dependency graph. You install modules in a component with `@InstallIn(Component::class)`; the component determines where bindings are available and how long they live.

| Component | Lifetime | Use Case |
|-----------|----------|----------|
| `SingletonComponent` | App lifetime | HTTP client, config, database |
| `ViewModelComponent` | Per ViewModel | Screen state, repository per screen |
| `NavigationComponent` | Per navigation destination | Destination-level state, helpers |
| Custom component | Your lifecycle | Activity scope, session scope, etc. |

## Scopes and Binding Kinds

A **scope** controls how long an instance lives and how many instances exist.

| Kind | Annotation | Lifetime | Where to Resolve |
|------|------------|----------|------------------|
| **Unscoped** | *(none)* | New instance every request | Anywhere (root or scope) |
| **Singleton** | `@Singleton` | One instance for the app | Anywhere; cached at root |
| **ViewModel-scoped** | `@ViewModelScoped` / `@InstallIn(ViewModelComponent::class)` | One per ViewModel | Only inside ViewModel scope |
| **Navigation-scoped** | `@NavigationScoped` / `@InstallIn(NavigationComponent::class)` | One per destination | Only inside navigation scope |
| **Custom-scoped** | `@Scoped(MyScope::class)` / `@InstallIn(MyScope::class)` | One per scope instance | Only inside that scope |

## Scope Rules

1. **Root (no scope):** `Anchor.inject<T>()` or `container.get<T>()` on the root. Only **unscoped** and **singleton** bindings can be resolved.
2. **Inside a scope:** Use `Anchor.withScope(Scope::class) { scope -> scope.get<T>() }` or `Anchor.scopedContainer(Scope::class)`.
3. **ViewModel scope:** Resolve ViewModel-scoped types only via `viewModelAnchor()` for ViewModels — not `Anchor.inject<ViewModel>()`.
4. **Navigation scope:** Resolve navigation-scoped types only inside `NavigationScopedContent` with `navigationScopedInject()`.
5. **Do not inject scoped types into longer-lived types** — e.g. don't inject ViewModel-scoped into a singleton.

## Constructor Injection

```kotlin
@Singleton
class UserRepository @Inject constructor(
    private val api: UserApi
)
```

The container resolves `UserApi` and passes it to the constructor. Dependencies can be interfaces, abstract classes, or concrete types — as long as there is a binding.
