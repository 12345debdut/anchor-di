# Troubleshooting

This page covers common issues you might encounter when using Anchor DI and how to fix them. If you run into something not listed here, consider opening an [issue](https://github.com/12345debdut/anchor-di/issues) on GitHub.

---

## "Scoped binding for X requires a scope"

**What it means:** A type is bound to a scope (e.g. `ViewModelComponent`) but was requested from the **root** container (no scope active). Anchor DI can't resolve scoped types from the root — they must be resolved inside the correct scope.

**How to fix:**

1. **ViewModel-scoped types**  
   Create the ViewModel with **`viewModelAnchor()`**, not `viewModel { Anchor.inject<MyViewModel>() }`. Only `viewModelAnchor()` runs resolution inside `Anchor.withScope(ViewModelComponent::class)`.

2. **Navigation-scoped types**  
   Resolve only inside `NavigationScopedContent(navBackStackEntry)` with `navigationScopedInject<T>()`. Don't call `Anchor.inject<T>()` from the root for navigation-scoped types.

3. **Custom-scoped types**  
   Resolve only inside `Anchor.withScope(YourScope::class) { scope -> scope.get<X>() }` or from a container returned by `Anchor.scopedContainer(YourScope::class)`.

4. **Alternative**  
   If the type doesn't need to be scoped, install its module in `SingletonComponent` instead of `ViewModelComponent` (or the other scoped component).

---

## "No binding found for X"

**What it means:** There is no binding for type `X` — no `@Inject` constructor, no `@Provides`, and no `@Binds` that provides `X`. The container doesn't know how to create or provide it.

**How to fix:**

1. **Add a binding**  
   - Use `@Inject` on the constructor if you control the class.  
   - Or add a `@Provides` method in a module.  
   - Or add a `@Binds` method to map an interface to an implementation.

2. **Ensure the module is installed**  
   The module must be `@InstallIn(SingletonComponent::class)` (or the appropriate component) and be part of the generated contributor. If you use multi-module, ensure the module is included in `getAnchorContributors()`.

3. **Rebuild**  
   KSP runs at compile time. Changes to annotations, modules, or dependencies require a **full rebuild** (e.g. `./gradlew clean build` or your IDE's rebuild).

---

## getAnchorContributors() not found

**What it means:** KSP hasn't generated the contributor yet, or the generated code isn't visible to the source set you're compiling.

**How to fix:**

1. **Rebuild**  
   Run a full build so KSP can process your code and generate `getAnchorContributors()`.

2. **iOS: Use correct source sets**  
   KSP generates code into target-specific directories that `iosMain` cannot see. You need an `actual` for `getAnchorContributors()` in **both** `iosArm64Main` and `iosSimulatorArm64Main` (not `iosMain`). See [Platform-Specific Setup](installation/platform-specific).

3. **Check KSP configuration**  
   Ensure you've added `anchor-di-ksp` for each Kotlin target you use (`kspCommonMainMetadata`, `kspAndroid`, `kspIosArm64`, etc.).

---

## Duplicate binding

**What it means:** Two modules (or a module and an `@Inject` class) provide the same type with the same qualifier. The container doesn't know which one to use.

**How to fix:**

- Add **qualifiers** to distinguish them: `@Named("id")` or a custom qualifier (e.g. `@ApiUrl`, `@WebUrl`).  
- Or remove one of the bindings if it's redundant.

---

## Circular dependency

**What it means:** A depends on B, B depends on C, C depends on A. The container can't resolve the cycle.

**How to fix:**

- **Restructure** — Break the cycle by introducing an interface, a new abstraction, or moving logic to a different layer.  
- **Use Lazy** — Sometimes injecting `Lazy<T>` can break a cycle (e.g. A depends on `Lazy<B>`, B depends on A). Use sparingly; prefer restructuring.

---

## iOS: Scope resolution fails on device or simulator

**What it means:** On Kotlin/Native (iOS), scope IDs might not match if the scope type isn't top-level or if there are reflection limitations.

**How to fix:**

- Ensure `getAnchorContributors()` is in **both** `iosArm64Main` and `iosSimulatorArm64Main`, not `iosMain`.  
- Use **top-level** `object` or `class` for scope markers (e.g. `object ActivityScope`).  
- Avoid nested scope types — they can have unstable qualified names on Kotlin/Native.

---

## Gradle / Build issues

**KSP version:** Use KSP 2.3+ compatible with your Kotlin version. Check the [KSP compatibility table](https://kotlinlang.org/docs/ksp-overview.html#supported-languages).

**Multi-module:** Each module that has `@Inject` classes or `@Module` classes needs `anchor-di-ksp`. Use `anchorDiModuleId` (e.g. `ksp { arg("anchorDiModuleId", "feature") }`) so each module generates a unique contributor. Aggregate contributors in your app module's `getAnchorContributors()`.

---

## Still stuck?

- Check the [documentation](https://12345debdut.github.io/anchor-di/) for detailed guides.  
- Search [existing issues](https://github.com/12345debdut/anchor-di/issues) on GitHub.  
- Open a [new issue](https://github.com/12345debdut/anchor-di/issues/new) with a minimal repro, error message, and environment (Kotlin version, KSP version, platform).
