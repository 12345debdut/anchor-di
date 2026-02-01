---
sidebar_position: 1
---

# Components and Scopes

This page explains the core concepts of Anchor DI: **components** and **scopes**. Understanding these will help you design a clean dependency graph and avoid common pitfalls. If you're new to DI or coming from Hilt/Dagger, these concepts will feel familiar.

---

## What is a Component?

A **component** is an entry point into the dependency graph. Think of it as a "container" that holds bindings. You install modules in a component with `@InstallIn(Component::class)`.

The component you choose determines:

1. **Where** its bindings are available (e.g. only inside ViewModel scope)
2. **How long** scoped instances live (e.g. one per ViewModel, one per app)

### Built-in Components

| Component | Lifetime | When to Use |
|-----------|----------|-------------|
| **SingletonComponent** | Application-wide — one instance for the whole app | HTTP client, database, config, platform context, logging, analytics. Anything that should live as long as the app. |
| **ViewModelComponent** | One instance per ViewModel | Screen state, repository used only by one screen, use cases that are screen-specific. |
| **NavigationComponent** | One instance per navigation destination (cleared when the destination is popped) | Destination-level state, screen-specific helpers, destination analytics. |
| **Custom component** | Your lifecycle (e.g. per Activity, per session) | Use when built-in scopes don't match your needs — e.g. one scope per Activity, per user session, or per flow. |

---

## What is a Scope?

A **scope** controls how long an instance lives and how many instances exist. When you request a type, the container looks at its binding: is it unscoped (new each time), singleton (one for the app), or scoped (one per scope instance)?

Scoping helps you:

- **Share state** within a boundary (e.g. one repository per ViewModel)
- **Avoid leaks** (don't hold short-lived objects in long-lived ones)
- **Control memory** (don't create a new HTTP client for every screen)

### Binding Kinds

| Kind | Annotation | Lifetime | Where to Resolve |
|------|------------|----------|------------------|
| **Unscoped** | *(none)* | New instance on every request | Anywhere (root or inside a scope) |
| **Singleton** | `@Singleton` | One instance for the whole app | Anywhere; cached at the root |
| **ViewModel-scoped** | `@ViewModelScoped` or `@InstallIn(ViewModelComponent::class)` | One instance per ViewModel | Only inside ViewModel scope (e.g. via `viewModelAnchor()`) |
| **Navigation-scoped** | `@NavigationScoped` or `@InstallIn(NavigationComponent::class)` | One instance per navigation destination | Only inside navigation scope (e.g. `NavigationScopedContent` + `navigationScopedInject()`) |
| **Custom-scoped** | `@Scoped(MyScope::class)` or `@InstallIn(MyScope::class)` | One instance per scope instance | Only inside that scope (`Anchor.withScope(MyScope::class) { }` or `Anchor.scopedContainer(MyScope::class)`) |

---

## Scope Rules (Important!)

Anchor DI enforces rules at compile time and runtime to keep your dependency graph consistent.

### Rule 1: Root (No Scope)

When you call `Anchor.inject<T>()` or `container.get<T>()` on the **root** container (no scope active), only **unscoped** and **singleton** bindings can be resolved.

If you try to resolve a ViewModel-scoped or navigation-scoped type from the root, you'll get: *"Scoped binding for X requires a scope."*

### Rule 2: Inside a Scope

When you run code inside `Anchor.withScope(Scope::class) { scope -> scope.get<T>() }` or hold a container from `Anchor.scopedContainer(Scope::class)`, that container has a **current scope ID**. Bindings scoped to that same scope are created once per scope and cached. Bindings from **parent** scopes (e.g. singleton) are still visible — the container delegates to its parent when the requested binding isn't for the current scope.

### Rule 3: ViewModel Scope

ViewModels created with `viewModelAnchor()` are created **inside** `ViewModelComponent` scope. So the ViewModel and everything it injects (including ViewModel-scoped types like a repository from a module `@InstallIn(ViewModelComponent::class)`) see the same scope.

If you create the ViewModel with `viewModel { Anchor.inject<MyViewModel>() }`, resolution runs at the **root** — ViewModel-scoped dependencies will fail. **Always use `viewModelAnchor()` for ViewModels that need ViewModel-scoped dependencies.**

### Rule 4: Navigation Scope

When content is wrapped in `NavigationScopedContent(navBackStackEntry)` (Android), that Composable provides a container with `NavigationComponent` scope. Calls to `navigationScopedInject<T>()` inside that content resolve from that container. Outside that content, `navigationScopedInject()` will fail.

### Rule 5: Don't Inject Scoped Types into Longer-Lived Types

Do **not** inject a ViewModel-scoped type into a singleton, or a navigation-scoped type into a ViewModel. The longer-lived object would hold a reference to the scope, leading to leaks or stale state. The dependency graph is validated at compile time where possible.

---

## Constructor Injection in Practice

The most common way to declare dependencies is constructor injection:

```kotlin
@Singleton
class UserRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun loadUser(id: String) = api.getUser(id)
}
```

**What happens:**

1. You request `UserRepository` (e.g. `Anchor.inject<UserRepository>()`).
2. The container looks up the binding for `UserRepository`.
3. It sees an `@Inject` constructor with parameter `UserApi`.
4. It resolves `UserApi` (from a binding in a module).
5. It creates `UserRepository` with that `UserApi` instance and returns it.

Dependencies can be interfaces, abstract classes, or concrete types — as long as there is a binding for each type.

---

## Quick Reference: Where to Resolve Each Kind

| Kind | How to Resolve |
|------|----------------|
| Singleton | `Anchor.inject<T>()`, or inject into any type |
| Unscoped | `Anchor.inject<T>()`, or inject into any type |
| ViewModel-scoped | Inside ViewModel created with `viewModelAnchor()`; inject into `@AnchorViewModel` classes |
| Navigation-scoped | Inside `NavigationScopedContent` with `navigationScopedInject<T>()` |
| Custom-scoped | Inside `Anchor.withScope(MyScope::class) { scope -> scope.get<T>() }` or from `Anchor.scopedContainer(MyScope::class)` |

---

## Next Steps

- **[Built-in Scopes](../scopes/built-in)** — Deep dive into Singleton, ViewModel, and Navigation scopes with examples
- **[Creating Custom Scopes](../scopes/custom-scopes)** — Define your own scopes (e.g. Activity, session)
