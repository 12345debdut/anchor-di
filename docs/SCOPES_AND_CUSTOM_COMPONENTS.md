# Scopes and Custom Components — Architecture and Lessons Learned

This document explains how scope resolution works in Anchor DI, why the previous custom-component support caused runtime failures, and what we must get right before reintroducing custom components.

---

## 1. Scope Resolution — The Critical Path

### 1.1 How a Scoped Binding Is Resolved

1. **Request**: Something calls `container.get<T>()` (e.g. to create a ViewModel that depends on `GreetingRepository`).

2. **Lookup**: The container looks up the binding for `T`'s `Key`. The binding can be:
   - `Unscoped` — create a new instance each time; **which container is passed to the factory matters**.
   - `Singleton` — create once, cache in root; container is the root.
   - `Scoped(scopeClassName, factory)` — create once **per scope**; only resolve if we're "in" that scope.

3. **Scope check** (for `Binding.Scoped`):
   - The container has a `currentScopeId: String?` (e.g. `"com.debdut.anchordi.ViewModelComponent"`).
   - The binding has `scopeClassName: String` (emitted by KSP in generated code).
   - **If `currentScopeId == binding.scopeClassName`** → we're in the right scope: create/cache in this container's `scopedCache` and return.
   - **If not and `parent != null`** → delegate to parent (e.g. resolve from parent scope).
   - **If not and `parent == null`** → we're at the root and the binding requires a scope → **throw**: *"Scoped binding for X requires a scope. Use Anchor.withScope(...) { ... } to provide the scope."*

4. **Container passed to factories**: For `Unscoped` and `Scoped` (when in scope), the factory receives a container via `resolveContainer(binding)`:
   - **Unscoped**: always `this` (the container that is resolving the request).
   - **Scoped**: `this` if `currentScopeId == binding.scopeClassName`, else `parent ?: this`.

So when we resolve `MainViewModel` (Unscoped) from a **scoped container** (ViewModel scope), the factory receives **that scoped container**. When the factory then calls `container.get<GreetingRepository>()`, it's still the scoped container — so `GreetingRepository` (Scoped to ViewModelComponent) resolves correctly **only if** the scoped container's `currentScopeId` equals the binding's `scopeClassName`.

**Conclusion**: The string used as the scope ID in generated code **must be identical** to the string used at runtime when creating the scope (`scopeClass.qualifiedName` or a constant). Any mismatch causes "requires a scope" at runtime.

---

## 2. Where the Scope ID Comes From

### 2.1 Current Design (Built-in Components Only)

| Layer        | Source of scope ID |
|-------------|---------------------|
| **KSP**     | **Literal** for ViewModel: `viewModelScopeClass = "com.debdut.anchordi.ViewModelComponent"`. Emitted as `Binding.Scoped("com.debdut.anchordi.ViewModelComponent", factory)`. |
| **Runtime** | `ViewModelComponent::class.qualifiedName` when we call `Anchor.withScope(ViewModelComponent::class) { ... }`. On JVM this is `"com.debdut.anchordi.ViewModelComponent"`. |

So for ViewModel we have:
- **Single source of truth in KSP**: a literal string.
- **Runtime**: `KClass.qualifiedName` for that same class.

On JVM they match. On Kotlin/Native they usually match for top-level objects. On Kotlin/JS, `KClass.qualifiedName` can be `null` (reflection limitations), so scope-by-class can be fragile.

### 2.2 What We Did for Custom Components (and Why It Broke)

When we added custom component support we:

1. **Stopped using literals for ViewModel**  
   We derived the scope ID for **all** components (including ViewModelComponent) from the annotation:
   - `componentQualifiedName = getScopedClassName(installIn)`
   - `getScopedClassName` does: `annotation.arguments.firstOrNull()?.value?.toString()` then `replace("class ", "").substringBefore(" ").trim()`.

2. **Used that derived string as the scope ID**  
   So even for `@InstallIn(ViewModelComponent::class)`, the emitted scope ID was whatever string we got from the annotation value's `toString()`, not the literal `"com.debdut.anchordi.ViewModelComponent"`.

3. **Runtime still used** `ViewModelComponent::class`  
   So runtime scope ID was still `ViewModelComponent::class.qualifiedName`.

**Why this caused "requires a scope"**:

- The annotation argument is a **KClass reference**. Its `toString()` in KSP is not guaranteed to match `KClass.qualifiedName` at runtime. It can differ by platform, or by how the type is represented (e.g. object vs class, or internal naming).
- So we had **two sources of truth**: KSP (from `toString()` of the annotation value) vs runtime (`KClass.qualifiedName`). They can disagree → `currentScopeId != binding.scopeClassName` → error.

So the bug was not “custom components are wrong” in principle — it was **relying on an unstable, string-derived scope ID** (from annotation `toString()`) that did not match the runtime scope ID (from `KClass.qualifiedName`).

---

## 3. Principles for Scope IDs

1. **One source of truth**  
   The same scope ID string must be used in:
   - Generated code: `Binding.Scoped("...", factory)`.
   - Runtime: `AnchorContainer(..., currentScopeId = "...")` when entering the scope.

2. **Built-in components**  
   Keep using **literals** in KSP for SingletonComponent and ViewModelComponent. Do **not** derive their scope IDs from the annotation value. That way:
   - KSP always emits the same string.
   - Runtime can use `ViewModelComponent::class.qualifiedName` (and on JVM/Native it matches the literal). Optionally we could later add a `ViewModelComponent.SCOPE_ID` constant and use that at runtime to avoid any `qualifiedName` quirks on JS.

3. **Custom components (future)**  
   For user-defined scopes we **must** derive the scope ID in a way that is **guaranteed to match** runtime `SomeScope::class.qualifiedName`:
   - **In KSP**: Resolve the `@InstallIn` argument to the **referenced class declaration** (e.g. `KSClassDeclaration`), then use `declaration.qualifiedName?.asString()`. That is the compiler’s qualified name, not `toString()` of the annotation value.
   - **At runtime**: The user enters the scope with `Anchor.withScope(MyScope::class) { ... }`, so `currentScopeId = MyScope::class.qualifiedName`. For this to match KSP, custom scope types must be **top-level** (class or object) so that `qualifiedName` is stable and the same as what KSP sees.

4. **Optional string-based scope API**  
   To avoid platform differences in `KClass.qualifiedName` (e.g. JS), we could add `Anchor.withScope(scopeId: String) { ... }` and document:
   - For built-in ViewModel scope, use a constant (e.g. `ViewModelComponent.SCOPE_ID`) in both KSP (literal) and runtime (constant).
   - For custom scopes, the user could pass a constant they control, and we’d document that it must match what KSP emits (the class’s qualified name).

---

## 4. Container and Factory Flow (Why ViewModel Scope Can Fail Without a Bug)

Even with the correct scope ID, the “requires a scope” error appears if a **scoped type is requested from the root container**:

- **Correct path**: Composable uses `viewModelAnchor<MainViewModel>()` → `viewModel { Anchor.withScope(ViewModelComponent::class) { it.get<MainViewModel>() } }` → scope is created → `get<MainViewModel>()` runs on the **scoped** container → MainViewModel’s factory receives the **scoped** container → `container.get<GreetingRepository>()` runs on that same scoped container → OK.
- **Wrong path**: Something calls `Anchor.inject<MainViewModel>()` or `viewModel { Anchor.inject<MainViewModel>() }` (no `withScope`) → `get<MainViewModel>()` runs on the **root** container → MainViewModel’s factory receives the **root** container → `container.get<GreetingRepository>()` runs on the root → GreetingRepository is Scoped(ViewModelComponent) but `currentScopeId` is null → **"requires a scope"**.

So we must ensure that **only** the path that runs inside `Anchor.withScope(ViewModelComponent::class)` (or equivalent) is used to create ViewModels that depend on ViewModel-scoped bindings. The current `viewModelAnchor()` does that; any other way of creating the ViewModel (or of resolving ViewModel-scoped types) must also run inside that scope.

---

## 5. Requirements Before Re-adding Custom Components

1. **KSP: Scope ID for custom components**
   - Resolve `@InstallIn(MyScope::class)` to the **referenced class symbol** (e.g. via the annotation argument’s type / declaration).
   - Use `referencedClass.qualifiedName?.asString()` as the **only** scope ID for that component. Do **not** use `getScopedClassName(installIn)` (annotation value `toString()`).
   - Keep **built-in** components (Singleton, ViewModel) on **literal** scope IDs in KSP; do not derive them from the annotation.

2. **API / docs**
   - Document that custom scope types must be **top-level** (so `qualifiedName` is stable and matches KSP).
   - Document that entering the scope must use the same scope (e.g. `Anchor.withScope(MyScope::class) { ... }`), and that the scope must be active whenever resolving types that are `@InstallIn(MyScope::class)` or `@Scoped(MyScope::class)`.

3. **Runtime**
   - No change strictly required: `createScope(scopeClass)` and `currentScopeId = scopeClass.qualifiedName` already support any scope class. Optionally add `withScope(scopeId: String)` and `scopedContainer(scopeId: String)` so apps can use a constant and avoid `KClass.qualifiedName` on platforms where it’s unreliable.

4. **Tests**
   - Add an integration test: enter ViewModel scope, resolve a ViewModel that depends on a ViewModel-scoped binding, assert success.
   - If we add custom components again: same test for a custom scope (enter scope, resolve a type that depends on a binding installed in that scope).

5. **No behavioral change for built-ins**
   - When adding custom component support, **do not** change how SingletonComponent or ViewModelComponent are detected or how their scope IDs are generated. Keep the current literal-based logic for them.

---

## 6. Summary

| Aspect | Lesson |
|--------|--------|
| **Scope ID** | Must be a single, consistent string between KSP and runtime. No “KSP from annotation toString()” vs “runtime from qualifiedName” mismatch. |
| **Built-in components** | Keep scope IDs as literals in KSP. Do not derive them from `@InstallIn` for Singleton/ViewModel. |
| **Custom components** | Derive scope ID from the **referenced class’s** `qualifiedName` in KSP (symbol resolution), not from annotation value `toString()`. Require top-level scope types. |
| **ViewModel path** | ViewModel-scoped bindings must only be resolved when the call runs inside `Anchor.withScope(ViewModelComponent::class)` (e.g. via `viewModelAnchor()`). |
| **Optional hardening** | String-based scope API + a `SCOPE_ID` constant for ViewModelComponent can avoid platform quirks of `KClass.qualifiedName`. |

Following this architecture keeps scope resolution predictable and makes custom components safe to reintroduce later.
