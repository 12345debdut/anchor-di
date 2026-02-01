# Troubleshooting

## "Scoped binding for X requires a scope"

**Cause:** A type is bound to a scope (e.g. `ViewModelComponent`) but was requested from the **root** container (no scope active).

**Fix:**

- **ViewModel-scoped types:** Create the ViewModel with **`viewModelAnchor()`**, not `viewModel { Anchor.inject<MyViewModel>() }`. Only `viewModelAnchor()` runs resolution inside `Anchor.withScope(ViewModelComponent::class)`.
- **Other scopes:** Resolve the type only inside `Anchor.withScope(YourScope::class) { scope -> scope.get<X>() }` or from a container returned by `Anchor.scopedContainer(YourScope::class)`.
- **Alternative:** If the type does not need to be scoped, install its module in `SingletonComponent` instead of `ViewModelComponent`.

## "No binding found for X"

**Cause:** There is no binding for type `X` — no `@Inject` constructor, no `@Provides`, and no `@Binds` that provides `X`.

**Fix:**

1. Add a binding (e.g. `@Inject` on the constructor, or a `@Provides` / `@Binds` in a module).
2. Ensure the module is `@InstallIn(SingletonComponent::class)` (or the appropriate component) and is included in your contributor.
3. **Rebuild** — KSP runs at compile time; changes require a full rebuild.

## KSP / Generated Code Issues

- **`getAnchorContributors()` not found:** Rebuild the project; KSP generates it. On iOS, ensure the actual is in both `iosArm64Main` and `iosSimulatorArm64Main`.
- **Duplicate binding:** Two modules (or a module and `@Inject`) provide the same type without a qualifier. Add `@Named` or a custom qualifier to distinguish them.
- **Circular dependency:** KSP validates cycles at compile time; fix the dependency graph.

## iOS: Scope resolution fails on device/simulator

- Ensure `getAnchorContributors()` is in **both** `iosArm64Main` and `iosSimulatorArm64Main`, not `iosMain`.
- Scope IDs use `KClass.qualifiedName`; on Kotlin/Native this can differ. Use top-level `object` or `class` for scope markers.

## Gradle / Build

- **KSP version:** Use KSP 2.3+ compatible with your Kotlin version.
- **Multi-module:** Each module with bindings needs `anchor-di-ksp`; use `anchorDiModuleId` for unique contributors.
